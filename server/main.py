import sys
import os
import datetime
import logging
from logging.handlers import RotatingFileHandler
import random
from flask import Flask, jsonify, request
from flask_cors import CORS

# =============================================================================
# НАСТРОЙКА ЛОГИРОВАНИЯ - ИСПРАВЛЕННАЯ ВЕРСИЯ
# =============================================================================

def setup_logging():
    """
    Настройка системы логирования БЕЗ зависимости от request context
    """
    # Создаем папку для логов если её нет
    if not os.path.exists('logs'):
        os.makedirs('logs')
    
    # Формат логов БЕЗ IP клиента в основном формате
    formatter = logging.Formatter(
        '%(asctime)s - %(levelname)s - [%(name)s] - %(message)s'
    )
    
    # Основной логгер
    logger = logging.getLogger('gpio_server')
    logger.setLevel(logging.INFO)
    
    # Файловый обработчик с ротацией
    file_handler = RotatingFileHandler(
        'logs/gpio_server.log',
        maxBytes=10*1024*1024,  # 10MB
        backupCount=5,
        encoding='utf-8'
    )
    file_handler.setFormatter(formatter)
    file_handler.setLevel(logging.INFO)
    
    # Консольный обработчик
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)
    console_handler.setLevel(logging.INFO)
    
    # Добавляем обработчики
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger

# Инициализация логирования ДО создания Flask app
logger = setup_logging()

# =============================================================================
# НАСТРОЙКА GPIO - АВТОМАТИЧЕСКОЕ ОПРЕДЕЛЕНИЕ ПЛАТФОРМЫ
# =============================================================================

try:
    # Пытаемся использовать настоящий GPIO для Raspberry Pi
    import RPi.GPIO as GPIO
    IS_RASPBERRY_PI = True
    logger.info("Инициализация: Настоящий Raspberry Pi - используем RPi.GPIO")
    
except (ImportError, RuntimeError) as e:
    # Режим разработки на ПК - используем симуляцию
    import fake_rpi
    fake_rpi.toggle_print(False)  # Отключаем лишние выводы
        
    # Подменяем системные модули для совместимости кода
    sys.modules['RPi'] = fake_rpi.RPi
    sys.modules['RPi.GPIO'] = fake_rpi.RPi.GPIO
    import RPi.GPIO as GPIO
        
    IS_RASPBERRY_PI = False
    logger.info("Инициализация: Режим разработки - используем fake-rpi")
    
        
    GPIO = MockGPIO
    IS_RASPBERRY_PI = False

# =============================================================================
# НАСТРОЙКА FLASK ПРИЛОЖЕНИЯ С УЧЕТОМ ANDROID КЛИЕНТА
# =============================================================================

app = Flask(__name__)

# 🔧 ВАЖНО: Настройка CORS для Android приложения
CORS(app, resources={
    r"/api/*": {
        "origins": ["*"],  # Разрешаем все источники для разработки
        "methods": ["GET", "POST", "PUT", "DELETE"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})

# Дополнительные настройки для мобильных клиентов
app.config['JSON_SORT_KEYS'] = False
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = True

# =============================================================================
# КОНФИГУРАЦИЯ GPIO ПИНОВ
# =============================================================================

# Настройка режима и пинов
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# 🔧 Номера пинов (BCM нумерация)
LED_PIN = 17          # Пин для светодиода
BUTTON_PIN = 18       # Пин для кнопки  
TEMPERATURE_SENSOR_PIN = 27  # Пин для датчика температуры
HUMIDITY_SENSOR_PIN = 22     # Пин для датчика влажности

# Инициализация пинов
GPIO.setup(LED_PIN, GPIO.OUT)
GPIO.setup(BUTTON_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(TEMPERATURE_SENSOR_PIN, GPIO.IN)
GPIO.setup(HUMIDITY_SENSOR_PIN, GPIO.IN)

logger.info(f"GPIO инициализирован в режиме: {'Raspberry Pi' if IS_RASPBERRY_PI else 'Разработка'}")

# =============================================================================
# ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ДЛЯ ЛОГИРОВАНИЯ
# =============================================================================

def log_android_request():
    """Логирует запросы от Android клиентов с IP адресом"""
    client_ip = request.remote_addr
    user_agent = request.headers.get('User-Agent', 'Unknown')
    
    logger.info(f"Android запрос: {request.method} {request.path} - IP: {client_ip} - User-Agent: {user_agent}")

def log_with_ip(message, level='info'):
    """Логирует сообщение с IP адресом клиента"""
    client_ip = getattr(request, 'remote_addr', 'unknown')
    log_message = f"{message} - IP: {client_ip}"
    
    if level == 'info':
        logger.info(log_message)
    elif level == 'warning':
        logger.warning(log_message)
    elif level == 'error':
        logger.error(log_message)

# =============================================================================
# MIDDLEWARE ДЛЯ ЛОГИРОВАНИЯ ЗАПРОСОВ ОТ ANDROID
# =============================================================================

@app.before_request
def before_request():
    """Логирует все входящие API запросы"""
    if request.path.startswith('/api/'):
        log_android_request()

# =============================================================================
# API ENDPOINTS ДЛЯ ANDROID КЛИЕНТА
# =============================================================================

@app.route('/')
def home():
    """
    Корневой endpoint - информация о сервере
    """
    log_with_ip("Запрос к корневому endpoint")
    return jsonify({
        "status": "success",
        "message": "🚀 Flask GPIO Server работает!",
        "service": "Raspberry Pi GPIO Control API",
        "version": "2.1",
        "platform": "raspberry_pi" if IS_RASPBERRY_PI else "development",
        "timestamp": datetime.datetime.utcnow().isoformat() + 'Z',
        "endpoints": {
            "health_check": "/health",
            "led_control": "/api/led/<state>",
            "button_status": "/api/button", 
            "sensor_data": "/api/sensors",
            "system_info": "/api/system"
        },
        "android_support": True,
        "cors_enabled": True
    })

@app.route('/health')
def health_check():
    """
    Health-check endpoint для мониторинга
    """
    logger.info("Health check запрос")
    try:
        health_data = {
            "status": "healthy",
            "service": "gpio-control-server",
            "timestamp": datetime.datetime.utcnow().isoformat() + 'Z',
            "uptime": get_uptime(),
            
            # Информация о системе
            "system": {
                "platform": "raspberry_pi" if IS_RASPBERRY_PI else "development",
                "python_version": sys.version.split()[0],
                "gpio_available": True,
            },
            
            # Статусы компонентов
            "components": {
                "led": "operational",
                "button": "operational", 
                "sensors": "operational",
                "api": "operational"
            },
            
            # Для Android клиента
            "mobile_support": {
                "android_compatible": True,
                "recommended_check_interval": 30  # секунды
            }
        }
        
        return jsonify(health_data), 200
        
    except Exception as e:
        logger.error(f"Health check ошибка: {str(e)}")
        return jsonify({
            "status": "unhealthy",
            "error": str(e),
            "timestamp": datetime.datetime.utcnow().isoformat() + 'Z'
        }), 500

@app.route('/api/led/<state>', methods=['POST', 'GET'])
def control_led(state):
    """
    Управление светодиодом - основной endpoint для Android клиента
    """
    try:
        log_with_ip(f"Управление LED: запрос состояния '{state}'")
        
        if state == 'on':
            GPIO.output(LED_PIN, GPIO.HIGH)
            response_data = {
                "status": "success",
                "action": "led_on",
                "message": "💡 Светодиод включен",
                "led_state": "ON",
                "timestamp": datetime.datetime.utcnow().isoformat() + 'Z'
            }
            log_with_ip("Светодиод включен")
            
        elif state == 'off':
            GPIO.output(LED_PIN, GPIO.LOW)
            response_data = {
                "status": "success", 
                "action": "led_off",
                "message": "🔌 Светодиод выключен",
                "led_state": "OFF",
                "timestamp": datetime.datetime.utcnow().isoformat() + 'Z'
            }
            log_with_ip("Светодиод выключен")
            
        elif state == 'status':
            # Проверка текущего состояния светодиода
            current_state = "ON" if GPIO.input(LED_PIN) == GPIO.HIGH else "OFF"
            response_data = {
                "status": "success",
                "action": "led_status",
                "led_state": current_state,
                "timestamp": datetime.datetime.utcnow().isoformat() + 'Z'
            }
            log_with_ip(f"Проверка состояния LED: {current_state}")
            
        else:
            log_with_ip(f"Неверное состояние LED: {state}", 'warning')
            return jsonify({
                "status": "error",
                "error_code": "INVALID_STATE",
                "message": "Неверное состояние. Используйте: 'on', 'off' или 'status'"
            }), 400
        
        return jsonify(response_data)
        
    except Exception as e:
        error_msg = f"Ошибка управления светодиодом: {str(e)}"
        log_with_ip(error_msg, 'error')
        return jsonify({
            "status": "error",
            "error_code": "GPIO_ERROR", 
            "message": error_msg
        }), 500

@app.route('/api/button', methods=['GET'])
def read_button():
    """
    Чтение состояния кнопки
    """
    try:
        button_state = GPIO.input(BUTTON_PIN)
        is_pressed = (button_state == GPIO.LOW)
        
        response_data = {
            "status": "success",
            "button_pressed": is_pressed,
            "button_state": "pressed" if is_pressed else "released",
            "timestamp": datetime.datetime.utcnow().isoformat() + 'Z'
        }
        
        # Логируем только при нажатии чтобы не засорять логи
        if is_pressed:
            log_with_ip("Кнопка нажата")
        
        return jsonify(response_data)
        
    except Exception as e:
        error_msg = f"Ошибка чтения кнопки: {str(e)}"
        log_with_ip(error_msg, 'error')
        return jsonify({
            "status": "error",
            "error_code": "BUTTON_READ_ERROR",
            "message": error_msg
        }), 500

@app.route('/api/sensors', methods=['GET'])
def read_sensors():
    """
    Чтение данных со всех сенсоров
    """
    try:
        # Симуляция данных датчиков для разработки
        if not IS_RASPBERRY_PI:
            temperature = round(20 + random.random() * 15, 1)  # 20-35°C
            humidity = round(30 + random.random() * 50, 1)     # 30-80%
            light_level = random.randint(0, 1023)
        else:
            # На реальном Pi здесь будет код для чтения настоящих датчиков
            temperature = 25.0
            humidity = 65.0
            light_level = 512
        
        sensor_data = {
            "status": "success",
            "timestamp": datetime.datetime.utcnow().isoformat() + 'Z',
            "sensors": {
                "temperature": {
                    "value": temperature,
                    "unit": "°C",
                    "sensor_type": "digital"
                },
                "humidity": {
                    "value": humidity, 
                    "unit": "%",
                    "sensor_type": "digital"
                },
                "light_level": {
                    "value": light_level,
                    "unit": "lux",
                    "sensor_type": "analog"
                }
            },
            "readings_count": 3
        }
        
        log_with_ip(f"Данные сенсоров: {temperature}°C, {humidity}%")
        return jsonify(sensor_data)
        
    except Exception as e:
        error_msg = f"Ошибка чтения сенсоров: {str(e)}"
        log_with_ip(error_msg, 'error')
        return jsonify({
            "status": "error",
            "error_code": "SENSOR_READ_ERROR",
            "message": error_msg
        }), 500

@app.route('/api/system', methods=['GET'])
def system_info():
    """
    Информация о системе для Android клиента
    """
    log_with_ip("Запрос информации о системе")
    system_info = {
        "status": "success",
        "server_info": {
            "name": "Raspberry Pi GPIO Server",
            "version": "2.1",
            "platform": "raspberry_pi" if IS_RASPBERRY_PI else "development",
            "uptime": get_uptime()
        },
        "gpio_configuration": {
            "led_pin": LED_PIN,
            "button_pin": BUTTON_PIN,
            "temperature_sensor_pin": TEMPERATURE_SENSOR_PIN,
            "humidity_sensor_pin": HUMIDITY_SENSOR_PIN,
            "pin_mode": "BCM"
        },
        "api_status": {
            "total_endpoints": 5,
            "android_support": True,
            "cors_enabled": True
        }
    }
    
    return jsonify(system_info)

# =============================================================================
# ОБРАБОТЧИКИ ОШИБОК ДЛЯ ANDROID КЛИЕНТА
# =============================================================================

@app.errorhandler(404)
def not_found_error(error):
    """Обработка 404 ошибок"""
    log_with_ip(f"404 Not Found: {request.path}", 'warning')
    return jsonify({
        "status": "error",
        "error_code": "ENDPOINT_NOT_FOUND",
        "message": f"Endpoint {request.path} не найден",
        "available_endpoints": ["/", "/health", "/api/led/<state>", "/api/button", "/api/sensors", "/api/system"]
    }), 404

@app.errorhandler(500)
def internal_error(error):
    """Обработка 500 ошибок"""
    log_with_ip(f"500 Internal Server Error: {str(error)}", 'error')
    return jsonify({
        "status": "error", 
        "error_code": "INTERNAL_SERVER_ERROR",
        "message": "Внутренняя ошибка сервера"
    }), 500

@app.errorhandler(405)
def method_not_allowed(error):
    """Обработка 405 ошибок"""
    log_with_ip(f"405 Method Not Allowed: {request.method} {request.path}", 'warning')
    return jsonify({
        "status": "error",
        "error_code": "METHOD_NOT_ALLOWED", 
        "message": f"Метод {request.method} не разрешен для {request.path}"
    }), 405

# =============================================================================
# ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
# =============================================================================

def get_uptime():
    """Возвращает время работы сервера"""
    if hasattr(app, 'start_time'):
        uptime = datetime.datetime.utcnow() - app.start_time
        hours, remainder = divmod(uptime.total_seconds(), 3600)
        minutes, seconds = divmod(remainder, 60)
        return f"{int(hours)}h {int(minutes)}m {int(seconds)}s"
    return "unknown"

# =============================================================================
# ЗАПУСК СЕРВЕРА
# =============================================================================

if __name__ == '__main__':
    """
    Точка входа приложения
    """
    try:
        # Записываем время старта для uptime
        app.start_time = datetime.datetime.utcnow()
        
        # Информация о запуске
        print("=" * 60)
        print("🚀 ЗАПУСК FLASK GPIO СЕРВЕРА")
        print("=" * 60)
        print(f"Платформа: {'Raspberry Pi' if IS_RASPBERRY_PI else 'Development PC'}")
        print(f"Python: {sys.version.split()[0]}")
        print("Доступные endpoints:")
        print("  GET  /              - Информация о сервере")
        print("  GET  /health        - Проверка здоровья")
        print("  POST/GET /api/led/<state> - Управление светодиодом")
        print("  GET  /api/button    - Состояние кнопки") 
        print("  GET  /api/sensors   - Данные сенсоров")
        print("  GET  /api/system    - Информация о системе")
        print("=" * 60)
        
        print("\n🌟 Сервер запускается...")
        print("📱 Готов к подключению Android устройств!")
        print("📍 URL для подключения:")
        print("   http://localhost:5000/")
        print("   http://[IP-адрес-компьютера]:5000/")
        print("\n⚡ Для остановки сервера нажмите Ctrl+C\n")
        
        # Записываем в лог информацию о запуске
        logger.info("Сервер успешно запущен")
        
        # 🔧 Production настройки
        app.run(
            host='0.0.0.0',      # Принимать подключения со всех интерфейсов
            port=5000,           # Порт по умолчанию для Flask
            debug=False,         # Выключить debug режим для production
            threaded=True        # Многопоточность для одновременных подключений
        )
        
    except KeyboardInterrupt:
        logger.info("Сервер остановлен пользователем (Ctrl+C)")
        print("\n👋 Сервер остановлен")
        
    except Exception as e:
        logger.critical(f"Критическая ошибка запуска: {str(e)}")
        print(f"❌ Ошибка запуска: {e}")
        
    finally:
        # Всегда очищаем GPIO при выходе
        GPIO.cleanup()
        logger.info("GPIO очищен")
        print("✅ GPIO очищен")