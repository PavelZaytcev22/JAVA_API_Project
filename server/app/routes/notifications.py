from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import Dict

from ..database import get_db
from ..crud import save_push_token, delete_push_token
from ..auth import get_current_user

router = APIRouter(prefix="/api/notifications", tags=["notifications"])

@router.post("/push-token")
async def register_push_token(
    token_data: dict,
    current_user: Dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Регистрация FCM токена для текущего пользователя
    """
    try:
        token = token_data.get("token")
        device_type = token_data.get("device_type", "unknown")
        device_name = token_data.get("device_name", "unknown")
        
        if not token:
            raise HTTPException(status_code=400, detail="Токен обязателен")
        
        save_push_token(db, current_user["id"], token, device_type, device_name)
        
        return {"status": "success", "message": "Токен успешно зарегистрирован"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка регистрации токена: {str(e)}")

@router.delete("/push-token")
async def unregister_push_token(
    token_data: dict,
    current_user: Dict = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Удаление FCM токена
    """
    try:
        token = token_data.get("token")
        
        if not token:
            raise HTTPException(status_code=400, detail="Токен обязателен")
        
        delete_push_token(db, token)
        
        return {"status": "success", "message": "Токен успешно удален"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка удаления токена: {str(e)}")