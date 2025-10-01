import json
import random
import requests
from models.database import db
from models.device import Device
from models.log import Log
from utils.gpio_setup import setup_gpio

class DeviceManager:
    def __init__(self, mqtt_service=None):
        self.GPIO, self.IS_RASPBERRY_PI = setup_gpio()
        self.mqtt_service = mqtt_service
        self.devices = {}
        
    def setup_devices(self):
        """Настройка устройств из базы данных"""
        devices = Device.query.all()
        for device in devices:
            self._setup_device(device)
        print(f"✓ Настроено {len(devices)} устройств")
    
    def _setup_device(self, device):
        """Настройка конкретного устройства"""
        if device.device_type == 'gpio':
            self._setup_gpio_device(device)
        elif device.device_type == 'zigbee':
            self._setup_zigbee_device(device)
        elif device.device_type == 'wifi':
            self._setup_wifi_device(device)
        else:
            print(f"⚠ Неизвестный тип устройства: {device.device_type}")
    
    def _setup_gpio_device(self, device):
        """Настройка GPIO устройства"""
        if device.pin is not None:
            # Исправляем: используем device_subtype вместо subtype
            if device.device_subtype in ['led', 'relay', 'buzzer']:
                self.GPIO.setup(device.pin, self.GPIO.OUT)
                self.GPIO.output(device.pin, self.GPIO.LOW)
                print(f"✓ Настроен GPIO выход: {device.name} ({device.device_subtype}) на пине {device.pin}")
            elif device.device_subtype in ['button', 'motion_sensor', 'temperature_sensor']:
                self.GPIO.setup(device.pin, self.GPIO.IN)
                print(f"✓ Настроен GPIO вход: {device.name} ({device.device_subtype}) на пине {device.pin}")
            else:
                print(f"⚠ Неизвестный подтип GPIO: {device.device_subtype}")
        
        self.devices[device.name] = device
    
    def _setup_zigbee_device(self, device):
        """Настройка Zigbee устройства"""
        print(f"✓ Настроено Zigbee устройство: {device.name} (топик: {device.topic})")
        self.devices[device.name] = device
    
    def _setup_wifi_device(self, device):
        """Настройка WiFi устройства"""
        print(f"✓ Настроено WiFi устройство: {device.name} (IP: {device.ip_address})")
        self.devices[device.name] = device
    
    def control_device(self, device_name, action, user_id):
        """Управление устройством"""
        if device_name not in self.devices:
            return {"error": "Device not found"}
        
        device = self.devices[device_name]
        
        try:
            if device.device_type == 'gpio':
                result = self._control_gpio_device(device, action)
            elif device.device_type == 'zigbee':
                result = self._control_zigbee_device(device, action)
            elif device.device_type == 'wifi':
                result = self._control_wifi_device(device, action)
            else:
                result = {"error": "Unsupported device type"}
            
            # Логирование действия
            self._log_action(user_id, device.id, f"control_{action}", 
                           "success" if "error" not in result else "error", 
                           str(result))
            
            return result
            
        except Exception as e:
            self._log_action(user_id, device.id, f"control_{action}", "error", str(e))
            return {"error": str(e)}
    
    def _control_gpio_device(self, device, action):
        """Управление GPIO устройством"""
        if action == 'on':
            self.GPIO.output(device.pin, self.GPIO.HIGH)
            device.state = 'on'
        elif action == 'off':
            self.GPIO.output(device.pin, self.GPIO.LOW)
            device.state = 'off'
        elif action == 'toggle':
            new_state = not (device.state == 'on')
            self.GPIO.output(device.pin, self.GPIO.HIGH if new_state else self.GPIO.LOW)
            device.state = 'on' if new_state else 'off'
        else:
            return {"error": "Invalid action"}
        
        db.session.commit()
        return {"status": "success", "state": device.state}
    
    def _control_zigbee_device(self, device, action):
        """Управление Zigbee устройством"""
        if self.mqtt_service and self.mqtt_service.is_connected:
            if action in ["on", "off"]:
                self.mqtt_service.publish(
                    f"zigbee2mqtt/{device.topic}/set", 
                    {"state": action.upper()}
                )
                device.state = action
                db.session.commit()
                return {"status": "success", "state": device.state}
            else:
                return {"error": "Invalid action"}
        else:
            return {"error": "MQTT not connected"}
    
    def _control_wifi_device(self, device, action):
        """Управление WiFi устройством"""
        try:
            response = requests.post(
                f"http://{device.ip_address}/control",
                json={"state": action},
                timeout=5
            )
            if response.status_code == 200:
                device.state = action
                db.session.commit()
                return {"status": "success", "state": device.state}
            else:
                return {"error": f"Device returned status {response.status_code}"}
        except requests.RequestException as e:
            return {"error": str(e)}
    
    def read_sensor(self, device_name, user_id):
        """Чтение данных с сенсора"""
        if device_name not in self.devices:
            return {"error": "Sensor not found"}
        
        device = self.devices[device_name]
        
        try:
            if device.device_subtype == 'temperature_sensor':
                result = self._read_temperature_sensor(device)
            elif device.device_subtype == 'motion_sensor':
                result = self._read_motion_sensor(device)
            elif device.device_subtype == 'button':
                result = self._read_button(device)
            else:
                result = {"error": "Cannot read this device type"}
            
            # Логирование
            self._log_action(user_id, device.id, "read_sensor", 
                           "success" if "error" not in result else "error")
            
            return result
            
        except Exception as e:
            self._log_action(user_id, device.id, "read_sensor", "error", str(e))
            return {"error": str(e)}
    
    def _read_temperature_sensor(self, device):
        """Чтение температуры (заглушка)"""
        temperature = round(20 + random.random() * 10, 1)
        humidity = round(30 + random.random() * 50, 1)
        
        return {
            "temperature": temperature,
            "humidity": humidity
        }
    
    def _read_motion_sensor(self, device):
        """Чтение датчика движения"""
        state = self.GPIO.input(device.pin)
        return {"motion": state == self.GPIO.HIGH}
    
    def _read_button(self, device):
        """Чтение состояния кнопки"""
        state = self.GPIO.input(device.pin)
        return {"pressed": state == self.GPIO.LOW}
    
    def _log_action(self, user_id, device_id, action, result, details=None):
        """Логирование действия"""
        log = Log(
            user_id=user_id,
            device_id=device_id,
            action=action,
            result=result,
            details=details
        )
        db.session.add(log)
        db.session.commit()
    
    def get_all_devices(self):
        """Получение всех устройств"""
        return {name: device.to_dict() for name, device in self.devices.items()}
    
    def cleanup(self):
        """Очистка ресурсов"""
        self.GPIO.cleanup()
        if self.mqtt_service:
            self.mqtt_service.disconnect()