from datetime import datetime, timedelta
from typing import Optional
from jose import jwt, JWTError
from passlib.context import CryptContext
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from . import models, database, config, crud

# =============================================================================
# НАСТРОЙКИ БЕЗОПАСНОСТИ И АУТЕНТИФИКАЦИИ
# =============================================================================

# Контекст для хеширования паролей с использованием Argon2=
pwd_context = CryptContext(schemes=["argon2"], deprecated="auto")

# Схема OAuth2 для извлечения токена из заголовков Authorization
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/token")


# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С ПАРОЛЯМИ
# =============================================================================

def get_password_hash(password: str) -> str:
    """
    Создание безопасного хеша пароля с помощью Argon2
    
    Args:
        password: пароль в чистом виде
        
    Returns:
        str: хеш пароля для хранения в базе данных
    """
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Проверка соответствия пароля его хешу
    
    Args:
        plain_password: пароль в чистом виде для проверки
        hashed_password: хеш из базы данных
        
    Returns:
        bool: True если пароль верный, False если неверный
    """
    return pwd_context.verify(plain_password, hashed_password)


# =============================================================================
# ФУНКЦИИ ДЛЯ РАБОТЫ С JWT ТОКЕНАМИ
# =============================================================================

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """
    Создание JWT токена доступа
    
    Args:
        data: данные для включения в payload токена (обычно {"sub": username})
        expires_delta: время жизни токена, по умолчанию из конфигурации
        
    Returns:
        str: закодированный JWT токен
    """
    # Копируем данные чтобы не изменять оригинал
    payload = data.copy()
    
    # Устанавливаем время expiration токена
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=config.ACCESS_TOKEN_EXPIRE_MINUTES)
    
    # Добавляем expiration в payload
    payload.update({"exp": expire})
    
    # Кодируем токен с использованием секретного ключа
    encoded_jwt = jwt.encode(
        payload, 
        config.JWT_SECRET, 
        algorithm=config.JWT_ALGORITHM
    )
    return encoded_jwt


def decode_access_token(token: str) -> Optional[dict]:
    """
    Декодирование и верификация JWT токена
    
    Args:
        token: JWT токен из заголовка Authorization
        
    Returns:
        Optional[dict]: payload токена если верификация успешна, иначе None
    """
    try:
        payload = jwt.decode(
            token, 
            config.JWT_SECRET, 
            algorithms=[config.JWT_ALGORITHM]
        )
        return payload
    except JWTError:
        # Токен невалиден: просрочен, неправильная подпись и т.д.
        return None


# =============================================================================
# ЗАВИСИМОСТИ (DEPENDENCI) FASTAPI
# =============================================================================

def get_db():
    """
    Dependency для получения сессии базы данных
    Гарантирует закрытие сессии после завершения запроса
    """
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()


def get_current_user(
    token: str = Depends(oauth2_scheme), 
    db: Session = Depends(get_db)
) -> models.User:
    """
    Dependency для получения текущего аутентифицированного пользователя
    Используется в защищенных маршрутах для проверки прав доступа
    
    Args:
        token: JWT токен из заголовка Authorization
        db: сессия базы данных
        
    Returns:
        models.User: объект пользователя если аутентификация успешна
        
    Raises:
        HTTPException: 401 если токен невалиден или пользователь не найден
    """
    # Стандартное исключение для ошибок аутентификации
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Не удалось подтвердить учетные данные",
        headers={"WWW-Authenticate": "Bearer"},
    )

    # Декодируем токен
    payload = decode_access_token(token)
    if payload is None:
        raise credentials_exception

    # Извлекаем username из payload (subject)
    username: str = payload.get("sub")
    if username is None:
        raise credentials_exception

    # Ищем пользователя в базе данных
    user = crud.get_user_by_username(db, username=username)
    if user is None:
        raise credentials_exception

    return user

def authenticate_super_admin(username: str, password: str) -> bool:
    """
    Аутентификация супер-администратора через переменные окружения
    """
    return (username == config.SUPER_ADMIN_USERNAME and 
            password == config.SUPER_ADMIN_PASSWORD)

def get_super_admin(
    token: str = Depends(oauth2_scheme)
):
    """
    Проверяет, является ли пользователь супер-администратором
    через JWT токен
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Неверные учетные данные супер-администратора",
        headers={"WWW-Authenticate": "Bearer"},
    )

    payload = decode_access_token(token)
    if payload is None:
        raise credentials_exception

    username: str = payload.get("sub")
    role: str = payload.get("role", "")
    
    if username != config.SUPER_ADMIN_USERNAME or role != "super_admin":
        raise credentials_exception

    # Возвращаем объект супер-админа (не из БД)
    return {
        "id": 0,  # Специальный ID для супер-админа
        "username": config.SUPER_ADMIN_USERNAME,
        "email": config.SUPER_ADMIN_EMAIL,
        "role": "super_admin",
        "is_super_admin": True
    }

