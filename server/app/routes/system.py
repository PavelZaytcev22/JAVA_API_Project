from fastapi import APIRouter, Depends
from .. import auth
from ..database import SessionLocal

# Создание роутера для системных endpoints
# Префикс /api/system добавляется ко всем маршрутам этого роутера
router = APIRouter(prefix="/api/system", tags=["system"])


@router.get("/ping")
def ping():
    """
    Endpoint для проверки работоспособности сервера
    
    Используется для:
    - Health check мониторингами
    - Проверки доступности сервера
    - Балансировщиками нагрузки
    
    Returns:
        dict: статус сервера и приветственное сообщение
    """
    return {
        "status": "ok", 
        "message": "Сервер работает",
        "service": "smart_home_server"
    }


@router.get("/me")
def get_current_user_info(current_user = Depends(auth.get_current_user)):
    """
    Получение информации о текущем аутентифицированном пользователе
    
    Требует валидный JWT токен в заголовке Authorization
    Используется для:
    - Проверки действительности токена
    - Получения данных пользователя в клиентских приложениях
    - Отладки аутентификации
    
    Args:
        current_user: объект пользователя из dependency injection
        
    Returns:
        dict: базовая информация о пользователе
    """
    return {
        "username": current_user.username,
        "full_name": current_user.full_name,
        "user_id": current_user.id
    }