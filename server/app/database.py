from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# URL для подключения к базе данных SQLite
# SQLite использует файл базы данных в текущей директории
SQLALCHEMY_DATABASE_URL = "sqlite:///./smart_home.db"

# Создание движка (engine) для работы с базой данных
# Движок управляет подключениями и выполняет SQL-запросы
engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    # Для SQLite необходимо отключить проверку одного потока,
    # так как FastAPI работает с несколькими потоками
    connect_args={"check_same_thread": False}
)

# Создание фабрики сессий (SessionLocal)
# Сессии используются для взаимодействия с базой данных
SessionLocal = sessionmaker(
    autocommit=False,    # Автоматический коммит отключен - контролируем вручную
    autoflush=False,     # Автоматический сброс отключен - контролируем вручную
    bind=engine          # Привязка к созданному движку
)

# Создание базового класса для всех моделей SQLAlchemy
# Все модели наследуются от этого класса и регистрируют свои таблицы
Base = declarative_base()