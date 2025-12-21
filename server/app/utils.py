import logging
from .config import LOG_LEVEL

def setup_logging():
    """
    Настройка системы логирования для приложения
    
    Конфигурирует:
    - Уровень логирования из переменных окружения
    - Формат вывода логов с временем, уровнем, именем модуля и сообщением
    - Уровни логирования для шумных библиотек
    """
    
    # Преобразуем строковый уровень логирования в объект уровня logging
    # По умолчанию используем INFO, если указан неверный уровень
    level = getattr(logging, LOG_LEVEL.upper(), logging.INFO)
    
    # Базовая конфигурация логирования
    logging.basicConfig(
        level=level,  # Уровень логирования для корневого логгера
        format=(
            "%(asctime)s %(levelname)s [%(name)s] %(message)s"
        ),  # Формат: время, уровень, имя_модуля, сообщение
        datefmt="%Y-%m-%d %H:%M:%S"  # Формат времени для логов
    )
    
    # Настройка уровня логирования для шумных библиотек
    # SQLAlchemy - уменьшаем уровень для запросов к БД
    logging.getLogger("sqlalchemy.engine").setLevel(logging.WARNING)
    
    # MQTT - устанавливаем информационный уровень для нашего MQTT модуля
    logging.getLogger("mqtt").setLevel(logging.INFO)
    
    # Дополнительные настройки для часто используемых библиотек
    logging.getLogger("urllib3").setLevel(logging.WARNING)  # HTTP запросы
    logging.getLogger("requests").setLevel(logging.WARNING)  # HTTP запросы
    
    # Логируем факт успешной настройки логирования
    logger = logging.getLogger(__name__)
    logger.info(
        "Система логирования настроена. Уровень: %s", 
        logging.getLevelName(level)
    )