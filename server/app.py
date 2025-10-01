import os
import sys

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from flask import Flask, jsonify
from flask_cors import CORS
from flask_jwt_extended import JWTManager, get_jwt_identity, jwt_required

from models.database import db, init_db
from models.user import User
from models.device import Device
from models.log import Log

from config import config
from services.device_manager import DeviceManager
from services.mqtt_service import MQTTService
from utils.logger import setup_logger

# Импортируем API routes
from api.auth import init_auth_routes
from api.devices import init_devices_routes

logger = setup_logger()

def create_app(config_name='default'):
    """Фабрика приложения"""
    app = Flask(__name__)
    app.config.from_object(config[config_name])
    
    # Инициализация расширений
    CORS(app)
    jwt = JWTManager(app)
    
    # Настройка JWT
    @jwt.unauthorized_loader
    def unauthorized_callback(callback):
        return jsonify({
            "status": "error",
            "message": "Missing authorization token"
        }), 401

    @jwt.invalid_token_loader
    def invalid_token_callback(callback):
        return jsonify({
            "status": "error", 
            "message": "Invalid token"
        }), 422

    @jwt.expired_token_loader
    def expired_token_callback(jwt_header, jwt_payload):
        return jsonify({
            "status": "error",
            "message": "Token has expired"
        }), 401

    @jwt.user_lookup_loader
    def user_lookup_callback(_jwt_header, jwt_data):
        identity = jwt_data["sub"]
        return User.query.get(identity)
    
    # Инициализация базы данных
    init_db(app)
    
    # Инициализация сервисов
    mqtt_service = MQTTService(None)
    device_manager = DeviceManager(mqtt_service)
    
    # Инициализация API routes
    init_auth_routes(app)
    init_devices_routes(app, device_manager)
    
    # Базовые маршруты
    @app.route('/')
    def home():
        return jsonify({
            "status": "success",
            "message": "Smart Home Server API",
            "version": "1.0.0",
            "gpio_mode": "real" if device_manager.IS_RASPBERRY_PI else "fake"
        })
    
    @app.route('/health')
    def health_check():
        return jsonify({
            "status": "healthy",
            "database": "connected",
            "mqtt": "connected" if mqtt_service.is_connected else "disconnected"
        })
    
    # Тестовый защищенный маршрут для проверки JWT
    @app.route('/api/protected')
    @jwt_required()
    def protected():
        current_user_id = get_jwt_identity()
        user = User.query.get(current_user_id)
        return jsonify({
            "status": "success",
            "user_id": current_user_id,
            "username": user.username if user else "Unknown"
        })
    
    # Создание тестовых данных
    with app.app_context():
        db.create_all()
        _create_test_data()
        device_manager.setup_devices()
        mqtt_service.connect()
    
    return app, device_manager

def _create_test_data():
    """Создание тестовых данных"""
    if not User.query.filter_by(username='admin').first():
        admin = User(username='admin', role='admin')
        admin.set_password('password')
        db.session.add(admin)
        
        devices = [
            Device(name='living_room_light', device_type='gpio', device_subtype='led', pin=17),
            Device(name='bedroom_light', device_type='gpio', device_subtype='led', pin=27),
            Device(name='door_button', device_type='gpio', device_subtype='button', pin=18),
            Device(name='motion_sensor', device_type='gpio', device_subtype='motion_sensor', pin=23),
            Device(name='temperature_sensor', device_type='gpio', device_subtype='temperature_sensor', pin=24),
            Device(name='buzzer', device_type='gpio', device_subtype='buzzer', pin=4),
            Device(name='relay', device_type='gpio', device_subtype='relay', pin=5),
            Device(name='zigbee_lamp', device_type='zigbee', device_subtype='light', topic='lamp_01'),
            Device(name='wifi_switch', device_type='wifi', device_subtype='switch', ip_address='192.168.1.100'),
        ]
        
        for device in devices:
            db.session.add(device)
        
        db.session.commit()
        logger.info("Test data created")

if __name__ == '__main__':
    try:
        app, device_manager = create_app()
        
        print("=" * 50)
        print("🚀 Smart Home Server Started")
        print(f"🔧 GPIO Mode: {'Raspberry Pi' if device_manager.IS_RASPBERRY_PI else 'Fake-RPi'}")
        print("📍 API: http://localhost:5000")
        print("🔐 Available endpoints:")
        print("   POST /api/login")
        print("   POST /api/register") 
        print("   GET  /api/devices")
        print("   POST /api/devices/{id}/control")
        print("   GET  /api/devices/{id}/read")
        print("   GET  /api/protected (test JWT)")
        print("=" * 50)
        
        app.run(host='0.0.0.0', port=5000, debug=True)
        
    except Exception as e:
        print(f"❌ Server error: {e}")
        import traceback
        traceback.print_exc()
    finally:
        if 'device_manager' in locals():
            device_manager.cleanup()
        print("✅ Server stopped")