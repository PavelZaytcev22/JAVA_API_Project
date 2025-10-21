import json
import logging
from apscheduler.schedulers.background import BackgroundScheduler
from .crud import get_all_automations, get_device, update_device_state
from .database import SessionLocal
from .notifications import send_push_to_user

# Настройка логгера для модуля автоматизаций
logger = logging.getLogger("automation")

# Инициализация фонового планировщика задач
_scheduler = BackgroundScheduler()
_scheduler.start()
logger.info("Фоновый планировщик автоматизаций запущен")


def notify_mqtt_event(db, device_id: int, state: str):
    """
    Обработка событий от MQTT для запуска автоматизаций
    Вызывается при изменении состояния устройства через MQTT
    
    Args:
        db: сессия базы данных
        device_id: ID устройства, которое изменило состояние
        state: новое состояние устройства
    """
    # Получаем все автоматизации из базы данных
    automations = get_all_automations(db)
    
    for automation in automations:
        # Пропускаем отключенные автоматизации и не подходящие по типу триггера
        if not automation.enabled or automation.trigger_type != "device_state":
            continue
            
        # Пытаемся разобрать условие срабатывания из JSON
        try:
            trigger_config = json.loads(automation.trigger_value or "{}")
            trigger_device_id = int(trigger_config.get("device_id", 0))
            trigger_state = str(trigger_config.get("state", ""))
            
            # Проверяем совпадение условия: устройство и состояние
            if trigger_device_id == device_id and trigger_state == str(state):
                logger.info("Сработала автоматизация: %s -> выполнение действия", automation.name)
                execute_action(automation, db)
                
        except Exception:
            logger.exception(
                "Ошибка парсинга trigger_value для автоматизации %s", 
                automation.id
            )


def execute_action(automation, db):
    """
    Выполнение действия автоматизации
    
    Args:
        automation: объект автоматизации из БД
        db: сессия базы данных
    """
    try:
        # Парсим действие из JSON строки
        action_config = json.loads(automation.action)
        target_device_id = int(action_config.get("device_id"))
        new_state = str(action_config.get("state"))
        
    except Exception:
        logger.exception(
            "Неверный формат action JSON для автоматизации %s", 
            automation.id
        )
        return
    
    # Находим целевое устройство
    target_device = get_device(db, target_device_id)
    if not target_device:
        logger.warning("Целевое устройство не найдено: %s", target_device_id)
        return
    
    # Обновляем состояние устройства в базе данных
    update_device_state(db, target_device, new_state)
    
    # Отправляем команду на устройство через MQTT
    from .mqtt_service import publish_device_state
    publish_device_state(target_device_id, new_state)
    
    # Отправляем push-уведомление владельцу дома
    try:
        owner_id = target_device.home.owner_id
        send_push_to_user(
            db, 
            owner_id, 
            f"Автоматизация: {automation.name}",
            f"Выполнено действие на устройстве {target_device.name}: {new_state}",
            data={"automation_id": automation.id}
        )
    except Exception:
        logger.exception("Ошибка отправки push-уведомления после автоматизации")


def load_scheduled_automations():
    """
    Загрузка и настройка запланированных автоматизаций в планировщик
    Вызывается при старте приложения
    """
    db = SessionLocal()
    try:
        automations = get_all_automations(db)
        
        for automation in automations:
            if not automation.enabled:
                continue
                
            # Обрабатываем только временные автоматизации с расписанием
            if automation.trigger_type == "time" and automation.schedule:
                _schedule_automation(automation)
                
    finally:
        db.close()


def _schedule_automation(automation):
    """
    Настройка расписания для временной автоматизации
    
    Args:
        automation: объект автоматизации с настройками расписания
    """
    schedule = automation.schedule
    
    # Поддержка интервальных автоматизаций (каждые N секунд)
    if schedule.startswith("interval:"):
        try:
            seconds = int(schedule.split(":", 1)[1])
            _scheduler.add_job(
                run_automation_job,
                "interval",
                seconds=seconds,
                args=[automation.id],
                id=f"auto_{automation.id}",
                replace_existing=True
            )
            logger.info(
                "Запланирована автоматизация %s каждые %s секунд", 
                automation.name, seconds
            )
            
        except ValueError:
            logger.error(
                "Неверный формат интервала для автоматизации %s: %s", 
                automation.id, schedule
            )
    else:
        # В минимальной реализации cron выражения не поддерживаются
        logger.warning(
            "Неподдерживаемый формат расписания для автоматизации %s: %s", 
            automation.id, schedule
        )


def run_automation_job(automation_id: int):
    """
    Задача для планировщика - выполнение автоматизации по расписанию
    
    Args:
        automation_id: ID автоматизации для выполнения
    """
    db = SessionLocal()
    try:
        # Получаем все автоматизации и находим нужную по ID
        all_automations = get_all_automations(db)
        automation_to_run = None
        
        for automation in all_automations:
            if automation.id == automation_id:
                automation_to_run = automation
                break
        
        # Выполняем автоматизацию если она включена
        if automation_to_run and automation_to_run.enabled:
            logger.info("Запуск запланированной автоматизации: %s", automation_to_run.name)
            execute_action(automation_to_run, db)
            
    finally:
        db.close()