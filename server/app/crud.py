from sqlalchemy import text
from sqlalchemy.orm import Session
from . import models, schemas, auth
from datetime import datetime

# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ (USERS)
# =============================================================================

def get_user_by_username(db: Session, username: str):
    """
    Поиск пользователя по имени пользователя
    Возвращает первого найденного пользователя или None
    """
    return db.query(models.User).filter(models.User.username == username).first()


def create_user(db: Session, user_in: schemas.UserCreate):
    """
    Создание нового пользователя с безопасным хешированием пароля
    """
    # Хеширование пароля
    hashed_password = auth.pwd_context.hash(user_in.password)

    # Создание объекта пользователя
    db_user = models.User(
        username=user_in.username,
        password_hash=hashed_password,
        email=getattr(user_in, "email", None)
    )
    
    # Сохранение в базе данных
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С PUSH-ТОКЕНАМИ
# =============================================================================

def add_push_token(db: Session, user_id: int, token: str):
    """
    Добавление FCM токена для пользователя
    Если токен уже существует, возвращает существующую запись
    """
    # Проверяем, существует ли уже такой токен для пользователя
    existing_token = db.query(models.PushToken).filter(
        models.PushToken.user_id == user_id, 
        models.PushToken.token == token
    ).first()
    
    if existing_token:
        return existing_token
    
    # Создаем новую запись токена
    new_token = models.PushToken(user_id=user_id, token=token)
    db.add(new_token)
    db.commit()
    db.refresh(new_token)
    return new_token


def get_push_tokens_for_user(db: Session, user_id: int):
    """
    Получение всех FCM токенов для пользователя
    Возвращает список токенов устройства
    """
    return db.query(models.PushToken).filter(models.PushToken.user_id == user_id).all()


# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С ДОМАМИ И КОМНАТАМИ
# =============================================================================

def add_home_member(db: Session, home_id: int, user_id: int):
    """Добавление пользователя в дом (без указания роли)"""
    existing = db.query(models.HomeMember).filter(
        models.HomeMember.home_id == home_id,
        models.HomeMember.user_id == user_id
    ).first()
    
    if existing:
        return existing
    
    home_member = models.HomeMember(home_id=home_id, user_id=user_id)
    db.add(home_member)
    db.commit()
    db.refresh(home_member)
    return home_member

def create_home(db: Session, owner_id: int, home_in: schemas.HomeCreate):
    """
    Создание нового дома для пользователя
    """
    home = models.Home(name=home_in.name, owner_id=owner_id)
    db.add(home)
    db.commit()
    db.refresh(home)

    # Создатель автоматически становится членом
    add_home_member(db, home.id, owner_id)

    return home

def get_homes_for_user(db: Session, owner_id: int):
    """
    Получение всех домов пользователя
    """
    return db.query(models.Home).filter(models.Home.owner_id == owner_id).all()

def get_home(db: Session, home_id: int):
    """Получение дома по ID"""
    return db.query(models.Home).filter(models.Home.id == home_id).first()

def get_user_homes(db: Session, user_id: int):
    """Получение всех домов пользователя (где он член)"""
    return db.query(models.Home).join(models.HomeMember).filter(
        models.HomeMember.user_id == user_id
    ).all()


def create_room(db: Session, home_id: int, room_in: schemas.RoomCreate):
    """
    Создание новой комнаты в доме
    """
    room = models.Room(name=room_in.name, home_id=home_id)
    db.add(room)
    db.commit()
    db.refresh(room)
    return room


def get_rooms_for_home(db: Session, home_id: int):
    """
    Получение всех комнат в доме
    """
    return db.query(models.Room).filter(models.Room.home_id == home_id).all()


# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С УСТРОЙСТВАМИ
# =============================================================================

def create_device(db: Session, home_id: int, device_in: schemas.DeviceCreate):
    """
    Создание нового устройства в доме
    """
    device = models.Device(
        name=device_in.name,
        type=device_in.type,
        room_id=device_in.room_id,
        state=device_in.state or "",
        home_id=home_id
    )
    db.add(device)
    db.commit()
    db.refresh(device)
    return device


def get_devices_for_home(db: Session, home_id: int):
    """
    Получение всех устройств в доме
    """
    return db.query(models.Device).filter(models.Device.home_id == home_id).all()


def get_device(db: Session, device_id: int):
    """
    Получение устройства по ID
    Возвращает устройство или None если не найдено
    """
    return db.query(models.Device).filter(models.Device.id == device_id).first()


def update_device_state(db: Session, device: models.Device, new_state: str):
    """
    Обновление состояния устройства
    Также обновляет время последнего изменения
    """
    device.state = new_state
    device.last_update = datetime.utcnow()
    db.commit()
    db.refresh(device)
    return device


def add_sensor_history(db: Session, device_id: int, value: str):
    """
    Добавление записи в историю изменений датчика
    """
    history_record = models.SensorHistory(device_id=device_id, value=value)
    db.add(history_record)
    db.commit()
    db.refresh(history_record)
    return history_record


# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С АВТОМАТИЗАЦИЯМИ
# =============================================================================

def create_automation(db: Session, owner_id: int, home_id: int, auto_in: schemas.AutomationCreate):
    """
    Создание новой автоматизации
    owner_id и home_id могут использоваться для проверки прав доступа
    """
    automation = models.Automation(
        name=auto_in.name,
        enabled=True,  # По умолчанию автоматизация включена
        trigger_type=auto_in.trigger_type,
        trigger_value=auto_in.trigger_value,
        action=auto_in.action,
        schedule=auto_in.schedule
    )
    db.add(automation)
    db.commit()
    db.refresh(automation)
    return automation


def get_all_automations(db: Session):
    """
    Получение всех автоматизаций из базы данных
    """
    return db.query(models.Automation).all()


def get_automation(db: Session, automation_id: int):
    """
    Получение автоматизации по ID
    """
    return db.query(models.Automation).filter(models.Automation.id == automation_id).first()


def set_automation_enabled(db: Session, automation: models.Automation, enabled: bool):
    """
    Включение или отключение автоматизации
    """
    automation.enabled = enabled
    db.commit()
    db.refresh(automation)
    return automation



def get_database_stats(db: Session):
    """Получение статистики базы данных"""
    from sqlalchemy import text
    
    stats = {}
    
    # Получаем список всех таблиц
    tables = db.execute(text("SELECT name FROM sqlite_master WHERE type='table'")).fetchall()
    
    for table in tables:
        table_name = table[0]
        count = db.execute(text(f"SELECT COUNT(*) FROM {table_name}")).scalar()
        stats[table_name] = count
        
    return stats

def execute_raw_sql(db: Session, sql: str):
    """Выполнение произвольного SQL запроса (ОПАСНО!)"""
    try:
        result = db.execute(text(sql))
        
        if sql.strip().lower().startswith('select'):
            # Для SELECT возвращаем результаты
            columns = result.keys()
            rows = result.fetchall()
            return {
                "columns": list(columns),
                "rows": [dict(zip(columns, row)) for row in rows]
            }
        else:
            # Для других запросов - коммитим изменения
            db.commit()
            return {"status": "executed", "rows_affected": result.rowcount}
            
    except Exception as e:
        db.rollback()
        raise e