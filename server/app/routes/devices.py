from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from .. import schemas, crud, auth, mqtt_service
from ..database import SessionLocal

router = APIRouter(prefix="/api/devices", tags=["devices"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/", response_model=schemas.Device)
def create_device(home_id: int, device_in: schemas.DeviceCreate, current_user = Depends(auth.get_current_user), db: Session = Depends(get_db)):
    # Optionally check that current_user owns the home
    device = crud.create_device(db, home_id=home_id, device_in=device_in)
    return device

@router.get("/", response_model=List[schemas.Device])
def list_devices(home_id: int, current_user = Depends(auth.get_current_user), db: Session = Depends(get_db)):
    devices = crud.get_devices_for_home(db, home_id=home_id)
    return devices

@router.post("/{device_id}/action")
def device_action(device_id: int, new_state: str, current_user = Depends(auth.get_current_user), db: Session = Depends(get_db)):
    device = crud.get_device(db, device_id)
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    updated = crud.update_device_state(db, device, new_state)
    # Publish via MQTT so Pi receives
    mqtt_service.publish_device_state(device_id, new_state)
    return {"status": "ok", "device": device_id, "state": new_state}
