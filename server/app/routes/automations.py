from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from .. import schemas, crud
from ..auth import get_current_user, get_db
from ..automation_service import load_scheduled_automations

router = APIRouter(prefix="/api/automations", tags=["automations"])

@router.post("/", response_model=schemas.AutomationOut)
def create_automation(auto_in: schemas.AutomationCreate, current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    # For simplicity, tie automation to owner/home not stored; permission checks omitted
    a = crud.create_automation(db, current_user.id, None, auto_in)
    # (re)load scheduled jobs
    load_scheduled_automations()
    return a

@router.get("/", response_model=List[schemas.AutomationOut])
def list_automations(current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    return crud.get_all_automations(db)

@router.post("/{automation_id}/enable")
def enable_automation(automation_id: int, enabled: bool, current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    a = crud.get_automation(db, automation_id)
    if not a:
        raise HTTPException(status_code=404, detail="Automation not found")
    updated = crud.set_automation_enabled(db, a, enabled)
    load_scheduled_automations()
    return {"status":"ok","automation":automation_id,"enabled":enabled}