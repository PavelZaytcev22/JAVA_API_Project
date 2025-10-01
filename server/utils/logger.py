import logging
from logging.handlers import RotatingFileHandler

def setup_logger(name='smart_home'):
    """Настройка логирования"""
    logger = logging.getLogger(name)
    logger.setLevel(logging.INFO)
    
    # Файловый handler
    file_handler = RotatingFileHandler(
        'smart_home.log', 
        maxBytes=1000000, 
        backupCount=5
    )
    file_handler.setFormatter(
        logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
    )
    
    # Консольный handler
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(
        logging.Formatter('%(name)s - %(levelname)s - %(message)s')
    )
    
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger