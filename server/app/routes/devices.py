from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List
from .. import schemas, crud, auth, mqtt_service
from ..auth import get_current_user, get_db

# Создание роутера для работы с устройствами
# Префикс /api/devices добавляется ко всем маршрутам этого роутера
router = APIRouter(prefix="/api/devices", tags=["devices"])


@router.post("/homes/{home_id}", response_model=schemas.DeviceOut)
def create_device(
    home_id: int,
    device_in: schemas.DeviceCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Создание нового устройства в указанном доме
    
    Args:
        home_id: ID дома, в котором создается устройство
        device_in: данные нового устройства (название, тип, комната, состояние)
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        schemas.DeviceOut: созданное устройство
        
    Raises:
        HTTPException: 403 если пользователь не является владельцем дома
        HTTPException: 404 если дом или комната не найдены
    """
    # TODO: реализовать проверку прав доступа - пользователь должен быть владельцем дома
    # TODO: проверить существование home_id и room_id (если указан)
    
    # Создание устройства в базе данных
    device = crud.create_device(db, home_id=home_id, device_in=device_in)
    
    return device


@router.get("/homes/{home_id}", response_model=List[schemas.DeviceOut])
def list_devices(
    home_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Получение списка всех устройств в указанном доме
    
    Args:
        home_id: ID дома для получения списка устройств
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        List[schemas.DeviceOut]: список устройств в доме
        
    Raises:
        HTTPException: 403 если пользователь не имеет доступа к дому
        HTTPException: 404 если дом не найден
    """
    # TODO: реализовать проверку прав доступа - пользователь должен иметь доступ к дому
    
    # Получение списка устройств из базы данных
    devices = crud.get_devices_for_home(db, home_id=home_id)
    
    return devices


@router.post("/{device_id}/action")
def device_action(
    device_id: int,
    new_state: str = Query(..., description="Новое состояние устройства (ON, OFF, значение)"),
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Выполнение действия над устройством (изменение состояния)
    
    Отправляет команду на физическое устройство через MQTT
    и обновляет состояние в базе данных
    
    Args:
        device_id: ID устройства для управления
        new_state: новое состояние устройства (например "ON", "OFF", "23.5")
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        dict: результат выполнения действия
        
    Raises:
        HTTPException: 403 если пользователь не имеет доступа к устройству
        HTTPException: 404 если устройство не найдено
    """
    # Получаем устройство из базы данных
    device = crud.get_device(db, device_id)
    if not device:
        raise HTTPException(
            status_code=404, 
            detail="Устройство не найдено"
        )
    
    # TODO: реализовать проверку прав доступа - пользователь должен иметь доступ к дому устройства
    
    # Обновляем состояние устройства в базе данных
    updated_device = crud.update_device_state(db, device, new_state)
    
    # Отправляем команду на физическое устройство через MQTT
    mqtt_service.publish_device_state(device_id, new_state)
    
    return {
        "status": "ok",
        "device": device_id,
        "state": new_state,
        "message": f"Команда успешно отправлена устройству {device.name}"
    }