from datetime import datetime, timedelta
from typing import Optional
from jose import jwt, JWTError
from passlib.context import CryptContext
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from . import models, database, config, crud, schemas

# Настройка хеширования пароля через argon2
pwd_context = CryptContext(schemes=["argon2"], deprecated="auto")

# OAuth2 схема для получения токена
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/token")


# -------------------------------
# Password hashing & verification
# -------------------------------
def get_password_hash(password: str) -> str:
    """Создание хеша пароля с помощью argon2"""
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверка пароля"""
    return pwd_context.verify(plain_password, hashed_password)


# -------------------------------
# JWT токены
# -------------------------------
def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """Создание JWT токена"""
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=config.ACCESS_TOKEN_EXPIRE_MINUTES))
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, config.JWT_SECRET, algorithm=config.JWT_ALGORITHM)
    return encoded_jwt


def decode_access_token(token: str) -> Optional[dict]:
    """Декодирование JWT токена"""
    try:
        payload = jwt.decode(token, config.JWT_SECRET, algorithms=[config.JWT_ALGORITHM])
        return payload
    except JWTError:
        return None


# -------------------------------
# Dependency для БД
# -------------------------------
def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()


# -------------------------------
# Получение текущего пользователя
# -------------------------------
def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    """Получение пользователя по токену"""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )

    payload = decode_access_token(token)
    if not payload:
        raise credentials_exception

    username: str = payload.get("sub")
    if username is None:
        raise credentials_exception

    user = crud.get_user_by_username(db, username=username)
    if not user:
        raise credentials_exception

    return user