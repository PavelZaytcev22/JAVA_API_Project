from pydantic import BaseModel
from typing import Any, Dict, Optional, List
from datetime import datetime

# =============================================================================
# МОДЕЛИ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ (USERS)
# =============================================================================

class UserCreate(BaseModel):
    """Модель для создания нового пользователя"""
    username: str           # Логин пользователя
    password: str           # Пароль (будет хешироваться)
    email: Optional[str] = None  # Email (необязательно)


class UserOut(BaseModel):
    """Модель для возврата данных о пользователе (без пароля)"""
    id: int
    username: str
    email: Optional[str] = None
    
    model_config = {"from_attributes": True}  # Разрешает создание из ORM моделей


class Token(BaseModel):
    """Модель для возврата JWT токена"""
    access_token: str      # JWT токен для аутентификации
    token_type: str        # Тип токена (обычно "bearer")


# =============================================================================
# МОДЕЛИ ДЛЯ УСТРОЙСТВ (DEVICES)
# =============================================================================

class DeviceBase(BaseModel):
    """Базовая модель устройства"""
    name: str              # Название устройства (например, "Свет в гостиной")
    type: str              # Тип устройства (например, "light", "sensor")
    room_id: Optional[int] = None  # ID комнаты, где находится устройство


class DeviceCreate(DeviceBase):
    """Модель для создания нового устройства"""
    state: Optional[str] = ""  # Начальное состояние устройства


class DeviceOut(DeviceBase):
    """Модель для возврата данных об устройстве"""
    id: int
    state: Optional[str] = ""          # Текущее состояние устройства
    last_update: Optional[datetime] = None  # Время последнего обновления
    home_id: int                       # ID дома, к которому привязано устройство
    
    model_config = {"from_attributes": True}


# =============================================================================
# МОДЕЛИ ДЛЯ КОМНАТ (ROOMS)
# =============================================================================

class RoomCreate(BaseModel):
    """Модель для создания новой комнаты"""
    name: str              # Название комнаты (например, "Гостиная", "Спальня")


class RoomOut(BaseModel):
    """Модель для возврата данных о комнате"""
    id: int
    name: str
    home_id: int           # ID дома, к которому привязана комната
    
    model_config = {"from_attributes": True}


# =============================================================================
# МОДЕЛИ ДЛЯ ДОМОВ (HOMES)
# =============================================================================

class HomeCreate(BaseModel):
    """Модель для создания нового дома"""
    name: str              # Название дома (например, "Моя квартира")


class FamilyMemberAdd(BaseModel):
    username: str  # username пользователя для добавления
    
class HomeMemberOut(BaseModel):
    id: int
    user_id: int
    home_id: int
    joined_at: datetime
    user: UserOut  # Информация о пользователе
    
    model_config = {"from_attributes": True}

class HomeOut(BaseModel):
    id: int
    name: str
    owner_id: int  # Оставляем для информации, но не для прав
    created_at: datetime
    members: List[HomeMemberOut] = []
    
    model_config = {"from_attributes": True}


# =============================================================================
# МОДЕЛИ ДЛЯ АВТОМАТИЗАЦИЙ (AUTOMATIONS)
# =============================================================================

class AutomationCreate(BaseModel):
    """Модель для создания новой автоматизации"""
    name: str                       # Название автоматизации
    trigger_type: str               # Тип триггера: "device_state" или "time"
    trigger_value: Optional[str] = None  # Значение триггера в формате JSON
    action: str                     # Действие в формате JSON
    schedule: Optional[str] = None  # Расписание (для временных триггеров)


class AutomationOut(BaseModel):
    """Модель для возврата данных об автоматизации"""
    id: int
    name: str
    enabled: bool                   # Включена ли автоматизация
    trigger_type: str               # Тип триггера
    trigger_value: Optional[str]    # Значение триггера
    action: str                     # Действие
    schedule: Optional[str]         # Расписание
    
    model_config = {"from_attributes": True}


# =============================================================================
# МОДЕЛИ ДЛЯ PUSH-УВЕДОМЛЕНИЙ
# =============================================================================

class PushTokenCreate(BaseModel):
    """Модель для регистрации FCM токена для push-уведомлений"""
    token: str  # FCM токен устройства




# Суперадмин
class SuperAdminLogin(BaseModel):
    username: str
    password: str

class DatabaseBackupResponse(BaseModel):
    status: str
    message: str
    backup_file: Optional[str] = None
    size_bytes: Optional[int] = None

class SystemInfoResponse(BaseModel):
    platform: str
    python_version: str
    hostname: str
    processor: str
    memory: Dict[str, Any]
    disk: Dict[str, Any]