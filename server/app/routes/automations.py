from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from .. import schemas, crud
from ..auth import get_current_user, get_db
from ..automation_service import load_scheduled_automations

# Создание роутера для работы с автоматизациями
# Префикс /api/automations добавляется ко всем маршрутам этого роутера
router = APIRouter(prefix="/api/automations", tags=["automations"])


@router.post("/", response_model=schemas.AutomationOut)
def create_automation(
    auto_in: schemas.AutomationCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Создание новой автоматизации
    
    Автоматизация может быть двух типов:
    - device_state: срабатывает при изменении состояния устройства
    - time: срабатывает по расписанию
    
    Args:
        auto_in: данные новой автоматизации (название, триггер, действие, расписание)
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        schemas.AutomationOut: созданная автоматизация
        
    Note:
        В текущей реализации привязка автоматизации к пользователю/дому не сохраняется в БД
        Необходимо добавить проверки прав доступа для продакшена
    """
    # TODO: добавить привязку автоматизации к пользователю и дому в базе данных
    # TODO: реализовать проверки прав доступа для создания автоматизаций
    
    # Создание автоматизации в базе данных
    automation = crud.create_automation(db, current_user.id, None, auto_in)
    
    # Перезагрузка запланированных задач в планировщике
    # Это необходимо для включения новой автоматизации в расписание
    load_scheduled_automations()
    
    return automation


@router.get("/", response_model=List[schemas.AutomationOut])
def list_automations(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Получение списка всех автоматизаций в системе
    
    Args:
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        List[schemas.AutomationOut]: список всех автоматизаций
        
    Note:
        В текущей реализации возвращаются все автоматизации без фильтрации по пользователю
        Для продакшена необходимо добавить фильтрацию по правам доступа
    """
    # TODO: добавить фильтрацию автоматизаций по пользователю или домам пользователя
    
    automations = crud.get_all_automations(db)
    return automations


@router.post("/{automation_id}/enable")
def enable_automation(
    automation_id: int,
    enabled: bool,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Включение или отключение автоматизации
    
    Args:
        automation_id: ID автоматизации для изменения состояния
        enabled: True - включить, False - отключить
        current_user: текущий аутентифицированный пользователь
        db: сессия базы данных
        
    Returns:
        dict: результат операции
        
    Raises:
        HTTPException: 404 если автоматизация не найдена
        HTTPException: 403 если пользователь не имеет прав на управление автоматизацией
    """
    # Получаем автоматизацию из базы данных
    automation = crud.get_automation(db, automation_id)
    if not automation:
        raise HTTPException(
            status_code=404, 
            detail="Автоматизация не найдена"
        )
    
    # TODO: реализовать проверку прав доступа - пользователь должен быть владельцем автоматизации
    
    # Обновляем состояние автоматизации (включена/отключена)
    updated_automation = crud.set_automation_enabled(db, automation, enabled)
    
    # Перезагружаем расписание в планировщике
    # Это необходимо для актуализации запланированных задач
    load_scheduled_automations()
    
    return {
        "status": "ok",
        "automation": automation_id,
        "enabled": enabled,
        "message": f"Автоматизация {'включена' if enabled else 'отключена'}"
    }