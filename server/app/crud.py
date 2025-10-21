from sqlalchemy.orm import Session
from . import models, schemas, auth
from datetime import datetime
import json

# USERS
def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user_in: schemas.UserCreate):
    hashed = auth.get_password_hash(user_in.password)
    db_user = models.User(username=user_in.username, hashed_password=hashed, full_name=user_in.full_name)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# PUSH TOKENS
def add_push_token(db: Session, user_id: int, token: str):
    existing = db.query(models.PushToken).filter(models.PushToken.user_id==user_id, models.PushToken.token==token).first()
    if existing:
        return existing
    obj = models.PushToken(user_id=user_id, token=token)
    db.add(obj)
    db.commit()
    db.refresh(obj)
    return obj

def get_push_tokens_for_user(db: Session, user_id: int):
    return db.query(models.PushToken).filter(models.PushToken.user_id==user_id).all()

# HOMES / ROOMS
def create_home(db: Session, owner_id: int, home_in: schemas.HomeCreate):
    home = models.Home(name=home_in.name, owner_id=owner_id)
    db.add(home)
    db.commit()
    db.refresh(home)
    return home

def get_homes_for_user(db: Session, owner_id: int):
    return db.query(models.Home).filter(models.Home.owner_id == owner_id).all()

def create_room(db: Session, home_id: int, room_in: schemas.RoomCreate):
    room = models.Room(name=room_in.name, home_id=home_id)
    db.add(room)
    db.commit()
    db.refresh(room)
    return room

def get_rooms_for_home(db: Session, home_id: int):
    return db.query(models.Room).filter(models.Room.home_id == home_id).all()

# DEVICES
def create_device(db: Session, home_id: int, device_in: schemas.DeviceCreate):
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
    return db.query(models.Device).filter(models.Device.home_id == home_id).all()

def get_device(db: Session, device_id: int):
    return db.query(models.Device).filter(models.Device.id == device_id).first()

def update_device_state(db: Session, device: models.Device, new_state: str):
    device.state = new_state
    device.last_update = datetime.utcnow()
    db.commit()
    db.refresh(device)
    return device

def add_sensor_history(db: Session, device_id: int, value: str):
    hist = models.SensorHistory(device_id=device_id, value=value)
    db.add(hist)
    db.commit()
    db.refresh(hist)
    return hist

# AUTOMATIONS
def create_automation(db: Session, owner_id: int, home_id: int, auto_in: schemas.AutomationCreate):
    # owner_id unused in db but could be used for permission checks
    auto = models.Automation(
        name=auto_in.name,
        enabled=True,
        trigger_type=auto_in.trigger_type,
        trigger_value=auto_in.trigger_value,
        action=auto_in.action,
        schedule=auto_in.schedule
    )
    db.add(auto)
    db.commit()
    db.refresh(auto)
    return auto

def get_all_automations(db: Session):
    return db.query(models.Automation).all()

def get_automation(db: Session, automation_id: int):
    return db.query(models.Automation).filter(models.Automation.id == automation_id).first()

def set_automation_enabled(db: Session, automation: models.Automation, enabled: bool):
    automation.enabled = enabled
    db.commit()
    db.refresh(automation)
    return automation