from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List
from .. import schemas, crud, auth, mqtt_service
from ..auth import get_current_user, get_db

router = APIRouter(prefix="/api/devices", tags=["devices"])

@router.post("/homes/{home_id}", response_model=schemas.DeviceOut)
def create_device(home_id: int, device_in: schemas.DeviceCreate, current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    device = crud.create_device(db, home_id=home_id, device_in=device_in)
    return device

@router.get("/homes/{home_id}", response_model=List[schemas.DeviceOut])
def list_devices(home_id: int, current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    devices = crud.get_devices_for_home(db, home_id=home_id)
    return devices

@router.post("/{device_id}/action")
def device_action(device_id: int, new_state: str = Query(...), current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    device = crud.get_device(db, device_id)
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    updated = crud.update_device_state(db, device, new_state)
    mqtt_service.publish_device_state(device_id, new_state)
    return {"status":"ok","device":device_id,"state":new_state}