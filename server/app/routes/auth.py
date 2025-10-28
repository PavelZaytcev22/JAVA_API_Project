from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .. import schemas, crud, auth
from ..auth import get_db

# Создание роутера для аутентификации и регистрации
# Префикс /api/auth добавляется ко всем маршрутам этого роутера
router = APIRouter(prefix="/api/auth", tags=["auth"])


@router.post("/register", response_model=schemas.UserOut)
def register(
    user_in: schemas.UserCreate,
    db: Session = Depends(get_db)
):
    """
    Регистрация нового пользователя в системе
    
    Создает учетную запись пользователя с безопасным хешированием пароля
    и возвращает данные пользователя без конфиденциальной информации
    
    Args:
        user_in: данные для регистрации (имя пользователя, пароль, полное имя)
        db: сессия базы данных
        
    Returns:
        schemas.UserOut: данные созданного пользователя (без пароля)
        
    Raises:
        HTTPException: 400 если имя пользователя уже занято
        HTTPException: 500 при ошибке создания пользователя
    """
    # Проверяем, не занято ли имя пользователя
    existing_user = crud.get_user_by_username(db, user_in.username)
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Имя пользователя уже занято"
        )
    
    # Создаем нового пользователя с хешированием пароля
    new_user = crud.create_user(db, user_in)
    
    return new_user


@router.post("/token", response_model=schemas.Token)
def login_for_token(
    user_in: schemas.UserCreate,
    db: Session = Depends(get_db)
):
    """
    Аутентификация пользователя и получение JWT токена
    
    Проверяет учетные данные и возвращает access token для использования
    в заголовках Authorization защищенных endpoints
    
    Args:
        user_in: учетные данные (имя пользователя и пароль)
        db: сессия базы данных
        
    Returns:
        schemas.Token: JWT access token и тип токена
        
    Raises:
        HTTPException: 401 если неверные учетные данные
    """
    # Ищем пользователя в базе данных
    user = crud.get_user_by_username(db, user_in.username)
    
    # Проверяем существование пользователя и корректность пароля
    if not user or not auth.verify_password(user_in.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверное имя пользователя или пароль",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # Создаем JWT токен с именем пользователя в payload
    access_token = auth.create_access_token({"sub": user.username})
    
    return {
        "access_token": access_token,
        "token_type": "bearer"
    }