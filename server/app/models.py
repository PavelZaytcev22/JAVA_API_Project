from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from .database import Base


class User(Base):
    """
    Модель пользователя системы
    Хранит учетные данные и основную информацию о пользователе
    """
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, nullable=False)      # Уникальное имя пользователя
    password_hash = Column(String(255), nullable=False)              # Хеш пароля
    email = Column(String(255), unique=True, nullable=True)          # Email (необязательно)
    created_at = Column(DateTime, default=datetime.utcnow)           # Дата регистрации

    # Связи с другими таблицами
    homes = relationship("Home", back_populates="owner")             # Дома пользователя


class Home(Base):
    """
    Модель дома/квартиры
    Основная единица организации умного дома
    """
    __tablename__ = "homes"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)                       # Название дома
    owner_id = Column(Integer, ForeignKey("users.id"))               # Владелец дома

    # Связи с другими таблицами
    owner = relationship("User", back_populates="homes")             # Владелец
    rooms = relationship("Room", back_populates="home")              # Комнаты в доме


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