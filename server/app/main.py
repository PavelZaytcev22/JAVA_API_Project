from fastapi import FastAPI
from contextlib import asynccontextmanager
from .database import Base, engine
from .routes import auth as auth_router, devices as devices_router, rooms as rooms_router, automations as autos_router, system as system_router, family as family_router, notifications as notifications_router
from .routes import sysadmin
from .mqtt_service import start_mqtt
from .utils import setup_logging
from .automation_service import load_scheduled_automations
import logging

# Настройка системы логирования при запуске приложения
setup_logging()
logger = logging.getLogger("app")

# Создание таблиц в базе данных (если они не существуют)
Base.metadata.create_all(bind=engine)
logger.info("Таблицы базы данных инициализированы")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Контекст жизненного цикла приложения
    Управляет запуском и остановкой фоновых служб
    """
    # Фаза запуска приложения
    logger.info("Запуск фоновых служб...")
    
    # Запуск MQTT клиента для связи с устройствами
    logger.info("Инициализация MQTT клиента...")
    start_mqtt()
    
    # Загрузка запланированных автоматизаций в планировщик
    logger.info("Загрузка запланированных автоматизаций...")
    try:
        load_scheduled_automations()
        logger.info("Автоматизации успешно загружены в планировщик")
    except Exception:
        logger.exception("Ошибка при загрузке запланированных автоматизаций")
    
    logger.info("Все службы успешно запущены")
    
    # Приложение работает
    yield
    
    # Фаза остановки приложения
    logger.info("Завершение работы приложения...")


# Создание основного экземпляра FastAPI приложения
app = FastAPI(
    title="Smart Home Server",
    version="1.0",
    description="Серверная часть системы умного дома с REST API и MQTT",
    lifespan=lifespan
)

# Подключение маршрутов (роутеров) API
# Порядок важен для приоритета маршрутов
app.include_router(auth_router.router)        # Аутентификация и авторизация
app.include_router(devices_router.router)     # Управление устройствами
app.include_router(rooms_router.router)       # Управление комнатами
app.include_router(autos_router.router)       # Автоматизации и сценарии
app.include_router(system_router.router)      # Системные endpoints
app.include_router(sysadmin.router)           # Админ системы
app.include_router(family_router.router)      # Управление семьей
app.include_router(notifications_router.router)

logger.info("Все маршруты API успешно подключены")


@app.get("/")
def root():
    """
    Корневой endpoint приложения
    Возвращает приветственное сообщение и статус сервеса
    """
    return {
        "message": "Добро пожаловать в Smart Home Server",
        "status": "работает",
        "version": "1.0"
    }


@app.get("/health")
def health_check():
    """
    Endpoint для проверки здоровья приложения
    Используется мониторингами и балансировщиками нагрузки
    """
    return {
        "status": "healthy",
        "service": "smart_home_server",
        "timestamp": datetime.utcnow().isoformat()
    }


# Добавляем импорт для health check endpoint
from datetime import datetime