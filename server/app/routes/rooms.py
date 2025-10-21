from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import schemas, crud
from ..auth import get_current_user, get_db

router = APIRouter(prefix="/api/rooms", tags=["rooms"])

@router.post("/homes/{home_id}", response_model=schemas.RoomOut)
def create_room(home_id: int, room_in: schemas.RoomCreate, current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    # TODO: check ownership of home
    room = crud.create_room(db, home_id, room_in)
    return room

@router.get("/homes/{home_id}", response_model=list[schemas.RoomOut])
def list_rooms(home_id: int, current_user = Depends(get_current_user), db: Session = Depends(get_db)):
    rooms = crud.get_rooms_for_home(db, home_id)
    return rooms