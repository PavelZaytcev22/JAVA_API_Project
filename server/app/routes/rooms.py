from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import schemas, crud
from ..auth import get_current_user, get_db

# Создание роутера для работы с комнатами
# Префикс /api/rooms добавляется ко всем маршрутам этого роутера
router = APIRouter(prefix="/api/rooms", tags=["rooms"])


@router.post("/homes/{home_id}", response_model=schemas.RoomOut)
def create_room(
    home_id: int,
    room_in: schemas.RoomCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Создание новой комнаты в указанном доме
    
    Args:
        home_id: ID дома, в котором создается комната
        room_in: данные новой комнаты (название)
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        schemas.RoomOut: созданная комната
        
    Raises:
        HTTPException: 403 если пользователь не является владельцем дома
        HTTPException: 404 если дом не найден
    """
    # TODO: реализовать проверку прав доступа - пользователь должен быть владельцем дома
    # Проверка что home_id существует и current_user является его владельцем
    
    # Создание комнаты в базе данных
    room = crud.create_room(db, home_id, room_in)
    
    return room


@router.get("/homes/{home_id}", response_model=list[schemas.RoomOut])
def list_rooms(
    home_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Получение списка всех комнат в указанном доме
    
    Args:
        home_id: ID дома для получения списка комнат
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        list[schemas.RoomOut]: список комнат в доме
        
    Raises:
        HTTPException: 403 если пользователь не имеет доступа к дому
        HTTPException: 404 если дом не найден
    """
    # TODO: реализовать проверку прав доступа - пользователь должен иметь доступ к дому
    # Проверка что home_id существует и current_user имеет к нему доступ
    
    # Получение списка комнат из базы данных
    rooms = crud.get_rooms_for_home(db, home_id)
    
    return rooms