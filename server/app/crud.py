from sqlalchemy.orm import Session
from . import models, schemas, auth
from datetime import datetime

# Users
def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user_in: schemas.UserCreate):
    hashed = auth.get_password_hash(user_in.password)
    db_user = models.User(username=user_in.username, hashed_password=hashed, full_name=user_in.full_name)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

# Homes
def create_home(db: Session, owner_id: int, home_in: schemas.HomeCreate):
    home = models.Home(name=home_in.name, owner_id=owner_id)
    db.add(home)
    db.commit()
    db.refresh(home)
    return home

def get_homes_for_user(db: Session, owner_id: int):
    return db.query(models.Home).filter(models.Home.owner_id == owner_id).all()

# Devices
def create_device(db: Session, home_id: int, device_in: schemas.DeviceCreate):
    device = models.Device(
        name=device_in.name,
        type=device_in.type,
        room=device_in.room,
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
