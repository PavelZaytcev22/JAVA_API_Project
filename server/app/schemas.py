from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

class UserCreate(BaseModel):
    username: str
    password: str
    full_name: Optional[str] = None

class UserOut(BaseModel):
    id: int
    username: str
    full_name: Optional[str] = None
    model_config = {"from_attributes": True}

class Token(BaseModel):
    access_token: str
    token_type: str

class DeviceBase(BaseModel):
    name: str
    type: str
    room_id: Optional[int] = None

class DeviceCreate(DeviceBase):
    state: Optional[str] = ""

class DeviceOut(DeviceBase):
    id: int
    state: Optional[str] = ""
    last_update: Optional[datetime] = None
    home_id: int
    model_config = {"from_attributes": True}

class RoomCreate(BaseModel):
    name: str

class RoomOut(BaseModel):
    id: int
    name: str
    home_id: int
    model_config = {"from_attributes": True}

class HomeCreate(BaseModel):
    name: str

class HomeOut(BaseModel):
    id: int
    name: str
    owner_id: int
    model_config = {"from_attributes": True}

class AutomationCreate(BaseModel):
    name: str
    trigger_type: str  # "device_state" or "time"
    trigger_value: Optional[str] = None  # JSON-string
    action: str  # JSON-string
    schedule: Optional[str] = None

class AutomationOut(BaseModel):
    id: int
    name: str
    enabled: bool
    trigger_type: str
    trigger_value: Optional[str]
    action: str
    schedule: Optional[str]
    model_config = {"from_attributes": True}

class PushTokenCreate(BaseModel):
    token: str