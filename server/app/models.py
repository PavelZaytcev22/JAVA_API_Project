from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from .database import Base

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String, nullable=True)

    homes = relationship("Home", back_populates="owner")

class Home(Base):
    __tablename__ = "homes"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"), nullable=False)

    owner = relationship("User", back_populates="homes")
    devices = relationship("Device", back_populates="home")

class Device(Base):
    __tablename__ = "devices"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    type = Column(String, nullable=False)
    room = Column(String, nullable=True)
    state = Column(Text, default="")  # JSON string or simple value
    last_update = Column(DateTime, default=datetime.utcnow)
    home_id = Column(Integer, ForeignKey("homes.id"), nullable=False)

    home = relationship("Home", back_populates="devices")
