from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from .database import Base
from sqlalchemy.sql import func


class User(Base):
    """
    Модель пользователя системы
    Хранит учетные данные и основную информацию о пользователе
    """
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, nullable=False)      # Уникальное имя пользователя
    password_hash = Column(String(255), nullable=False)              # Хеш пароля
    email = Column(String(255), unique=True, nullable=True) 
    role = Column(String(50), default="user")  # 🔐 'admin', 'user'         # Email (необязательно)
    created_at = Column(DateTime, default=datetime.utcnow)           # Дата регистрации

    # Обновленные связи
    owned_homes = relationship("Home", back_populates="owner")  # Дома где пользователь владелец
    home_memberships = relationship("HomeMember", back_populates="user")  # Членство в домах
    push_tokens = relationship("PushToken", back_populates="user", cascade="all, delete-orphan")


class Home(Base):
    __tablename__ = "homes"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))  # Главный владелец
    created_at = Column(DateTime, default=datetime.utcnow)

    # Связи
    owner = relationship("User", back_populates="owned_homes")
    members = relationship("HomeMember", back_populates="home")
    rooms = relationship("Room", back_populates="home")

class HomeMember(Base):
    """
    Таблица для связи пользователей и домов (многие-ко-многим)
    Определяет права доступа пользователей к домам
    """
    __tablename__ = "home_members"

    id = Column(Integer, primary_key=True, index=True)
    home_id = Column(Integer, ForeignKey("homes.id"))
    user_id = Column(Integer, ForeignKey("users.id"))
    joined_at = Column(DateTime, default=datetime.utcnow)

    # Связи
    home = relationship("Home", back_populates="members")
    user = relationship("User", back_populates="home_memberships")           # Комнаты в доме


class Room(Base):
    """
    Модель комнаты
    Группирует устройства по помещениям внутри дома
    """
    __tablename__ = "rooms"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)                       # Название комнаты
    home_id = Column(Integer, ForeignKey("homes.id"))                # Принадлежность к дому

    # Связи с другими таблицами
    home = relationship("Home", back_populates="rooms")              # Родительский дом
    devices = relationship("Device", back_populates="room")          # Устройства в комнате


class Device(Base):
    """
    Модель устройства умного дома
    Представляет физические устройства (лампы, датчики и т.д.)
    """
    __tablename__ = "devices"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)                       # Название устройства
    type = Column(String(100), nullable=False)                       # Тип (light, sensor, switch)
    status = Column(String(50), default="off")                       # Текущее состояние
    room_id = Column(Integer, ForeignKey("rooms.id"))                # Расположение в комнате

    # Связи с другими таблицами
    room = relationship("Room", back_populates="devices")            # Комната устройства


class Automation(Base):
    """
    Модель автоматизации
    Описывает правила автоматического управления устройствами
    """
    __tablename__ = "automations"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)                       # Название автоматизации
    trigger = Column(String(255), nullable=False)                    # Условие срабатывания
    action = Column(Text, nullable=False)                            # Выполняемое действие
    enabled = Column(Boolean, default=True)                          # Активна ли автоматизация
    created_at = Column(DateTime, default=datetime.utcnow)           # Дата создания


class Notification(Base):
    """
    Модель уведомлений
    Хранит историю отправленных push-уведомлений
    """
    __tablename__ = "notifications"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255))                                      # Заголовок уведомления
    message = Column(Text)                                           # Текст уведомления
    created_at = Column(DateTime, default=datetime.utcnow)           # Время отправки

class PushToken(Base):
    __tablename__ = "push_tokens"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"))
    token = Column(String, unique=True, index=True, nullable=False)
    device_type = Column(String)  # android, ios, web
    device_name = Column(String)  # Например: "Samsung Galaxy S21"
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Связь с пользователем
    user = relationship("User", back_populates="push_tokens")