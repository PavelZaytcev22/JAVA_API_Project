import sys
import os
import datetime
import json
import logging
from logging.handlers import RotatingFileHandler
from flask import Flask, jsonify, request
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from flask_socketio import SocketIO, emit
import random
import requests  # Для Wi-Fi устройств

# =============================================================================
# НАСТРОЙКА GPIO
# =============================================================================

try:
    import RPi.GPIO as GPIO
    IS_RASPBERRY_PI = True
except (ImportError, RuntimeError):
    from fake_rpi import toggle_print
    toggle_print(False)
    sys.modules['RPi'] = __import__('fake_rpi').RPi
    sys.modules['RPi.GPIO'] = __import__('fake_rpi').RPi.GPIO
    import RPi.GPIO as GPIO
    IS_RASPBERRY_PI = False

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# =============================================================================
# MQTT ДЛЯ ZIGBEE
# =============================================================================

import paho.mqtt.client as mqtt

# =============================================================================
# УПРАВЛЕНИЕ УСТРОЙСТВАМИ
# =============================================================================

class DeviceManager:
    def __init__(self, config_file="devices.json"):
        self.devices = {}
        self.config = self._load_config(config_file)
        self.mqtt_client = mqtt.Client()
        self.mqtt_client.on_connect = self._on_mqtt_connect
        self.mqtt_client.on_message = self._on_mqtt_message
        
        # Попытка подключения к MQTT брокеру с обработкой ошибок
        try:
            self.mqtt_client.connect("localhost", 1883, 60)
            self.mqtt_client.loop_start()
            print("✓ Успешно подключен к MQTT брокеру localhost:1883")
        except Exception as e:
            print(f"✗ Ошибка подключения к MQTT брокеру: {e}")
            print("  Запуск в оффлайн режиме...")
            self.mqtt_client = None
        
        self._setup_devices()

    def _load_config(self, config_file):
        if not os.path.exists(config_file):
            logger.warning(f"Config file {config_file} not found. Using default devices.")
            return {"devices": []}  # Или дефолтные устройства
        with open(config_file, 'r') as f:
            return json.load(f)

    def _setup_devices(self):
        for device in self.config.get('devices', []):
            dev_type = device.get('type')
            name = device.get('name')
            if dev_type == "led":
                self._setup_led(name, device.get('pin'))
            elif dev_type == "button":
                self._setup_button(name, device.get('pin'))
            elif dev_type == "motion_sensor":
                self._setup_motion_sensor(name, device.get('pin'))
            elif dev_type == "temperature_sensor":
                self._setup_temperature_sensor(name, device.get('pin'))
            elif dev_type == "buzzer":
                self._setup_buzzer(name, device.get('pin'))
            elif dev_type == "relay":
                self._setup_relay(name, device.get('pin'))
            elif dev_type == "zigbee":
                self._setup_zigbee(name, device.get('topic'))
            elif dev_type == "wifi":
                self._setup_wifi(name, device.get('ip'))
            else:
                logger.warning(f"Unknown device type: {dev_type}")

        # Дефолтные устройства, если конфиг пустой
        if not self.devices:
            self._setup_led("living_room_light", 17)
            self._setup_led("bedroom_light", 27)
            self._setup_button("door_button", 18)
            self._setup_motion_sensor("motion_sensor", 23)
            self._setup_temperature_sensor("temperature_sensor", 24)
            self._setup_buzzer("buzzer", 4)
            self._setup_relay("relay", 5)
            self._setup_zigbee("zigbee_lamp", "lamp_01")
            self._setup_wifi("wifi_switch", "192.168.1.100")

    def _setup_led(self, name, pin):
        GPIO.setup(pin, GPIO.OUT)
        GPIO.output(pin, GPIO.LOW)
        self.devices[name] = {"type": "led", "pin": pin, "state": False}

    def _setup_button(self, name, pin):
        GPIO.setup(pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        self.devices[name] = {"type": "button", "pin": pin}

    def _setup_motion_sensor(self, name, pin):
        GPIO.setup(pin, GPIO.IN)
        self.devices[name] = {"type": "motion_sensor", "pin": pin}

    def _setup_temperature_sensor(self, name, pin):
        GPIO.setup(pin, GPIO.IN)
        self.devices[name] = {"type": "temperature_sensor", "pin": pin, "temperature": 22.0, "humidity": 45.0}

    def _setup_buzzer(self, name, pin):
        GPIO.setup(pin, GPIO.OUT)
        GPIO.output(pin, GPIO.LOW)
        self.devices[name] = {"type": "buzzer", "pin": pin, "state": False}

    def _setup_relay(self, name, pin):
        GPIO.setup(pin, GPIO.OUT)
        GPIO.output(pin, GPIO.LOW)
        self.devices[name] = {"type": "relay", "pin": pin, "state": False}

    def _setup_zigbee(self, name, topic):
        self.devices[name] = {"type": "zigbee", "topic": topic, "state": False}

    def _setup_wifi(self, name, ip):
        self.devices[name] = {"type": "wifi", "ip": ip, "state": False}

    def _on_mqtt_connect(self, client, userdata, flags, rc):
        client.subscribe("zigbee2mqtt/#")
        logger.info("MQTT connected")

    def _on_mqtt_message(self, client, userdata, msg):
        logger.info(f"MQTT: {msg.topic} -> {msg.payload.decode()}")
        # Здесь можно обновить состояние устройств и отправить через SocketIO

    def control_device(self, device_id, action):
        if device_id not in self.devices:
            return {"error": "Device not found"}

        device = self.devices[device_id]
        dev_type = device["type"]

        if dev_type in ["led", "buzzer", "relay"]:
            return self._control_gpio_device(device, action, dev_type)
        elif dev_type == "zigbee":
            return self._control_zigbee_device(device, action)
        elif dev_type == "wifi":
            return self._control_wifi_device(device, action)
        else:
            return {"error": "Unsupported device type"}

    def _control_gpio_device(self, device, action, dev_type):
        pin = device["pin"]
        if action == "on":
            GPIO.output(pin, GPIO.HIGH)
            device["state"] = True
            return {"status": "success", "state": "on"}
        elif action == "off":
            GPIO.output(pin, GPIO.LOW)
            device["state"] = False
            return {"status": "success", "state": "off"}
        elif action == "toggle" and dev_type == "led":
            new_state = not device["state"]
            GPIO.output(pin, GPIO.HIGH if new_state else GPIO.LOW)
            device["state"] = new_state
            return {"status": "success", "state": "on" if new_state else "off"}
        else:
            return {"error": "Invalid action"}

    def _control_zigbee_device(self, device, action):
        if action in ["on", "off"]:
            self.mqtt_client.publish(f"zigbee2mqtt/{device['topic']}/set", json.dumps({"state": action.upper()}))
            device["state"] = (action == "on")
            return {"status": "success", "state": action}
        return {"error": "Invalid action"}

    def _control_wifi_device(self, device, action):
        try:
            response = requests.post(f"http://{device['ip']}/control", json={"state": action})
            if response.ok:
                device["state"] = (action == "on")
                return {"status": "success", "state": action}
            else:
                return {"error": "Wi-Fi device error"}
        except requests.RequestException as e:
            return {"error": str(e)}

    def read_sensor(self, device_id):
        if device_id not in self.devices:
            return {"error": "Sensor not found"}

        device = self.devices[device_id]
        dev_type = device["type"]

        if dev_type == "button":
            state = GPIO.input(device["pin"])
            return {"status": "success", "pressed": state == GPIO.LOW}
        elif dev_type == "motion_sensor":
            state = GPIO.input(device["pin"])
            return {"status": "success", "motion": state == GPIO.HIGH}
        elif dev_type == "temperature_sensor":
            device["temperature"] = round(20 + random.random() * 10, 1)
            device["humidity"] = round(30 + random.random() * 50, 1)
            return {
                "status": "success",
                "temperature": device["temperature"],
                "humidity": device["humidity"]
            }
        else:
            return {"error": "Cannot read this device type"}

    def get_device_info(self, device_id):
        return self.devices.get(device_id)

    def get_all_devices(self):
        return {k: v.copy() for k, v in self.devices.items()}  # Копируем, чтобы избежать мутации

# =============================================================================
# FLASK СЕРВЕР
# =============================================================================

app = Flask(__name__)
CORS(app)
app.config['JWT_SECRET_KEY'] = 'your-secret-key'  # Измените на реальный секрет
jwt = JWTManager(app)
socketio = SocketIO(app, cors_allowed_origins="*")

# Логирование
logger = logging.getLogger('smart_home')
logger.setLevel(logging.INFO)
handler = RotatingFileHandler('smart_home.log', maxBytes=1000000, backupCount=5)
handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
logger.addHandler(handler)

device_manager = DeviceManager()

# Уведомление через SocketIO
def notify_update(event, data):
    socketio.emit(event, data)

@socketio.on('connect')
def handle_connect():
    logger.info("Client connected")
    notify_update('status_update', device_manager.get_all_devices())

@app.route('/')
def home():
    return jsonify({
        "status": "success",
        "message": "Smart Home Server",
        "gpio_mode": "real" if IS_RASPBERRY_PI else "fake"
    })

@app.route('/api/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    
    # Простая проверка (в реальности используйте базу данных и хэширование)
    if username == 'admin' and password == 'password':
        access_token = create_access_token(identity=username)
        return jsonify(access_token=access_token)
    return jsonify({"msg": "Bad username or password"}), 401

@app.route('/api/devices')
@jwt_required()
def get_devices():
    devices = device_manager.get_all_devices()
    return jsonify({
        "status": "success",
        "devices": devices
    })

@app.route('/api/devices/<device_id>/control', methods=['POST'])
@jwt_required()
def control_device(device_id):
    if not request.is_json:
        return jsonify({"status": "error", "message": "JSON required"}), 400
    data = request.get_json()
    action = data.get('action')
    if not action or action not in ['on', 'off', 'toggle']:
        return jsonify({"status": "error", "message": "Invalid or missing action"}), 400
    
    logger.info(f"Control request for {device_id}: {action} by user {get_jwt_identity()}")
    result = device_manager.control_device(device_id, action)
    
    if "error" in result:
        return jsonify({"status": "error", "message": result["error"]}), 400
    
    notify_update('device_update', {"device_id": device_id, "result": result})
    return jsonify({
        "status": "success",
        "device": device_id,
        "action": action,
        "result": result
    })

@app.route('/api/devices/<device_id>/read')
@jwt_required()
def read_device(device_id):
    result = device_manager.read_sensor(device_id)
    
    if "error" in result:
        return jsonify({"status": "error", "message": result["error"]}), 400
    
    return jsonify({
        "status": "success",
        "device": device_id,
        "data": result
    })

@app.route('/api/devices/<device_id>/info')
@jwt_required()
def get_device_info(device_id):
    info = device_manager.get_device_info(device_id)
    
    if not info:
        return jsonify({"status": "error", "message": "Device not found"}), 404
    
    return jsonify({
        "status": "success",
        "device": device_id,
        "info": info
    })

if __name__ == '__main__':
    print("🚀 Smart Home Server Started")
    print(f"🔧 GPIO Mode: {'Raspberry Pi' if IS_RASPBERRY_PI else 'Fake-RPi'}")
    socketio.run(app, host='0.0.0.0', port=5000, debug=False, allow_unsafe_werkzeug=True)