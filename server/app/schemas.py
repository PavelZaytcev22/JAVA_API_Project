from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

# Pydantic v2: use model_config for from_attributes
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

class TokenData(BaseModel):
    username: Optional[str] = None

class DeviceBase(BaseModel):
    name: str
    type: str
    room: Optional[str] = None

class DeviceCreate(DeviceBase):
    state: Optional[str] = ""

class Device(DeviceBase):
    id: int
    state: Optional[str] = ""
    last_update: Optional[datetime] = None
    home_id: int

    model_config = {"from_attributes": True}

class HomeCreate(BaseModel):
    name: str

class HomeOut(BaseModel):
    id: int
    name: str
    owner_id: int

    model_config = {"from_attributes": True}
