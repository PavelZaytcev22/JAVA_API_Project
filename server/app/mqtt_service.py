import paho.mqtt.client as mqtt
from paho.mqtt.enums import CallbackAPIVersion
import logging
from .config import MQTT_BROKER, MQTT_PORT, MQTT_BASE_TOPIC
from .database import SessionLocal
from .crud import get_device, update_device_state, add_sensor_history
from .automation_service import notify_mqtt_event  # для запуска автоматизаций
import json

# Настройка логирования
logger = logging.getLogger("mqtt")

# Создание MQTT клиента
MQTT_CLIENT = mqtt.Client(
    client_id="smart_home_server", 
    callback_api_version=CallbackAPIVersion.VERSION2
)


def on_connect(client, userdata, flags, rc, properties=None):
    """
    Callback при подключении к MQTT брокеру
    """
    logger.info("Успешное подключение к MQTT брокеру. Код: %s", rc)
    
    # Подписываемся на все топики в рамках базового пути
    topic = f"{MQTT_BASE_TOPIC}/#"
    client.subscribe(topic)
    logger.info("Подписка на топик: %s", topic)


def on_message(client, userdata, msg):
    """
    Callback при получении сообщения из MQTT
    Обрабатывает сообщения от устройств и обновляет состояние в БД
    """
    try:
        topic = msg.topic
        payload = msg.payload.decode()
        logger.info("Получено MQTT сообщение: %s -> %s", topic, payload)
        
        # Парсим топик для извлечения device_id
        # Формат топика: base_topic/device/{device_id}/state
        parts = topic.split("/")
        
        # Ищем часть 'device' в пути топика
        if "device" in parts:
            device_idx = parts.index("device")
            try:
                # Следующая часть после 'device' - это ID устройства
                device_id = int(parts[device_idx + 1])
            except (ValueError, IndexError):
                logger.debug("Некорректный device_id в топике: %s", topic)
                return
            
            # Обновляем состояние устройства в базе данных
            _update_device_in_database(device_id, payload)
            
    except Exception:
        logger.exception("Ошибка при обработке MQTT сообщения")


def _update_device_in_database(device_id: int, payload: str):
    """
    Обновляет состояние устройства в базе данных
    и запускает связанные автоматизации
    """
    db = SessionLocal()
    try:
        # Получаем устройство из базы данных
        device = get_device(db, device_id)
        if not device:
            logger.warning("Неизвестный device_id: %s", device_id)
            return
        
        # Сохраняем историю изменений датчика
        add_sensor_history(db, device_id, payload)
        
        # Обновляем текущее состояние устройства
        update_device_state(db, device, payload)
        logger.info("Обновлено состояние устройства %s -> %s", device_id, payload)
        
        # Запускаем автоматизации, связанные с этим событием
        notify_mqtt_event(db, device_id, payload)
        
    except Exception:
        logger.exception("Ошибка при обновлении устройства в БД")
    finally:
        # Всегда закрываем соединение с БД
        db.close()


def start_mqtt():
    """
    Запускает MQTT клиент и подключается к брокеру
    """
    try:
        # Настройка callback функций
        MQTT_CLIENT.on_connect = on_connect
        MQTT_CLIENT.on_message = on_message
        
        # Подключение к брокеру
        MQTT_CLIENT.connect(MQTT_BROKER, MQTT_PORT, keepalive=60)
        
        # Запуск фонового цикла обработки сообщений
        MQTT_CLIENT.loop_start()
        logger.info("MQTT клиент успешно запущен")
        
    except Exception:
        logger.exception("Ошибка при запуске MQTT клиента")


def publish_device_state(device_id: int, state: str):
    """
    Публикует команду для устройства через MQTT
    
    Args:
        device_id: ID устройства
        state: новое состояние устройства (ON/OFF или другие значения)
    """
    topic = f"{MQTT_BASE_TOPIC}/device/{device_id}/cmd"
    
    try:
        MQTT_CLIENT.publish(topic, state)
        logger.info("Отправлена команда: %s -> %s", topic, state)
    except Exception:
        logger.exception("Ошибка при отправке команды устройству %s", device_id)