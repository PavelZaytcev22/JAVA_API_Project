import paho.mqtt.client as mqtt
import json
from flask_socketio import SocketIO

class MQTTService:
    def __init__(self, socketio: SocketIO):
        self.socketio = socketio
        self.client = mqtt.Client()
        self.client.on_connect = self._on_connect
        self.client.on_message = self._on_message
        self.is_connected = False
        
    def connect(self):
        """Подключение к MQTT брокеру"""
        try:
            self.client.connect("localhost", 1883, 60)
            self.client.loop_start()
            self.is_connected = True
            return True
        except Exception as e:
            print(f"✗ Ошибка подключения к MQTT: {e}")
            return False
    
    def _on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("✓ MQTT подключен")
            client.subscribe("zigbee2mqtt/#")
        else:
            print(f"✗ MQTT ошибка подключения: {rc}")
    
    def _on_message(self, client, userdata, msg):
        try:
            payload = msg.payload.decode()
            data = json.loads(payload)
            
            # Отправка обновления через WebSocket
            self.socketio.emit('mqtt_update', {
                'topic': msg.topic,
                'data': data
            })
            
        except Exception as e:
            print(f"Ошибка обработки MQTT сообщения: {e}")
    
    def publish(self, topic, message):
        """Публикация сообщения в MQTT"""
        if self.is_connected:
            self.client.publish(topic, json.dumps(message))
    
    def disconnect(self):
        """Отключение от MQTT"""
        if self.is_connected:
            self.client.loop_stop()
            self.client.disconnect()