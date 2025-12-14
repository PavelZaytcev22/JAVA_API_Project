#!/usr/bin/env python3
# mqtt_client.py - Подключение к MQTT брокеру с TLS

import paho.mqtt.client as mqtt
import ssl
import json
import time
import yaml
import os
import sys
from datetime import datetime

class RaspberryMQTTClient:
    def __init__(self, config_file="config.yaml"):
        """Инициализация MQTT клиента"""
        self.config = self.load_config(config_file)
        self.client = None
        self.connected = False
        self.start_time = time.time()
        
        print("=" * 50)
        print("RASPBERRY PI MQTT CLIENT")
        print("=" * 50)
        print(f"Брокер: {self.config['mqtt']['broker']}:{self.config['mqtt']['port']}")
        print(f"Client ID: {self.config['mqtt']['client_id']}")
        print(f"Пользователь: {self.config['mqtt']['username']}")
        print("=" * 50)
    
    def load_config(self, config_file):
        """Загрузка конфигурации из YAML файла"""
        try:
            with open(config_file, 'r') as f:
                config = yaml.safe_load(f)
            
            # Проверка обязательных полей
            required = ['broker', 'port', 'client_id', 'username', 'password']
            for field in required:
                if field not in config['mqtt']:
                    raise ValueError(f"Отсутствует обязательное поле: mqtt.{field}")
            
            return config
        except FileNotFoundError:
            print(f" Ошибка: Файл {config_file} не найден")
            sys.exit(1)
        except yaml.YAMLError as e:
            print(f" Ошибка в YAML файле: {e}")
            sys.exit(1)
    
    def on_connect(self, client, userdata, flags, rc):
        """Callback при подключении к брокеру"""
        if rc == 0:
            self.connected = True
            print(f"✅ [{self.get_timestamp()}] Успешное подключение к брокеру!")
            
            # Подписываемся на топики
            for topic in self.config['topics']['subscribe']:
                client.subscribe(topic, qos=self.config['mqtt']['qos'])
                print(f"   📫 Подписан на: {topic}")
            
            # Отправляем статус "online"
            self.publish_status("online", "system_started")
            
        else:
            error_codes = {
                1: "Неверная версия протокола",
                2: "Неверный client_id",
                3: "Сервер недоступен",
                4: "Неверные логин/пароль",
                5: "Нет прав на подключение"
            }
            error_msg = error_codes.get(rc, f"Неизвестная ошибка (код: {rc})")
            print(f"❌ [{self.get_timestamp()}] Ошибка подключения: {error_msg}")
    
    def on_message(self, client, userdata, msg):
        """Callback при получении сообщения"""
        try:
            payload = msg.payload.decode('utf-8')
            print(f"📨 [{self.get_timestamp()}] Получено сообщение:")
            print(f"   Топик: {msg.topic}")
            print(f"   Данные: {payload[:100]}{'...' if len(payload) > 100 else ''}")
            
            # Пытаемся разобрать JSON
            data = json.loads(payload)
            self.handle_json_command(msg.topic, data)        
        except Exception as e:
            print(f"⚠️ Ошибка обработки сообщения: {e}")
    
    def on_disconnect(self, client, userdata, rc):
        """Callback при отключении"""
        self.connected = False
        if rc != 0:
            print(f" [{self.get_timestamp()}] Неожиданное отключение. Попытка переподключения...")
    
    def on_publish(self, client, userdata, mid):
        """Callback при успешной публикации"""
        print(f" [{self.get_timestamp()}] Сообщение опубликовано (ID: {mid})")
    
    def handle_json_command(self, topic, data):
        """Обработка JSON команд"""
        print(f" Обработка JSON команды")
        print(f" Команда: {json.dumps(data, indent=2)}")
        
        # Пример обработки команд
        action = data.get('action', '').lower()
        
        if action == 'ping':
            response = {
                "response": "pong",
                "timestamp": time.time(),
                "client_id": self.config['mqtt']['client_id']
            }
            self.client.publish(f"{topic}/response", json.dumps(response))
            
        elif action == 'get_status':
            self.publish_full_status()
            
        elif action in ['turn_on', 'turn_off']:
            device = data.get('device', 'unknown')
            print(f"   ⚡ Управление устройством: {device} -> {action}")
            # Здесь можно добавить управление GPIO
            
            # Отправляем подтверждение
            ack = {
                "status": "success",
                "action": action,
                "device": device,
                "timestamp": time.time()
            }
            self.client.publish(f"kitchen/device/ack", json.dumps(ack))
    
    def publish_status(self, status, reason=""):
        """Публикация статуса устройства"""
        if not self.connected:
            return
            
        status_msg = {
            "client_id": self.config['mqtt']['client_id'],
            "status": status,
            "reason": reason,
            "timestamp": time.time(),
            "uptime": round(time.time() - self.start_time, 2),
            "device_info": self.config['device']
        }
        
        topic = self.config['topics']['publish']['status']
        self.client.publish(
            topic,
            payload=json.dumps(status_msg, indent=2),
            qos=self.config['mqtt']['qos'],
            retain=self.config['mqtt']['retain_messages']
        )
        
        print(f"📊 [{self.get_timestamp()}] Статус опубликован: {status}")
    
    def publish_full_status(self):
        """Публикация полной информации о системе"""
        import platform
        
        full_status = {
            "client_id": self.config['mqtt']['client_id'],
            "status": "online",
            "timestamp": time.time(),
            "system": {
                "hostname": platform.node(),
                "python_version": platform.python_version(),
                "platform": platform.platform(),
                "uptime": round(time.time() - self.start_time, 2)
            },
            "mqtt_config": {
                "broker": self.config['mqtt']['broker'],
                "connected": self.connected,
                "topics_subscribed": len(self.config['topics']['subscribe'])
            },
            "device": self.config['device']
        }
        
        self.client.publish(
            self.config['topics']['publish']['status'],
            payload=json.dumps(full_status, indent=2),
            qos=self.config['mqtt']['qos']
        )
    
    def setup_last_will(self):
        """Настройка Last Will сообщения"""
        last_will_msg = json.dumps({
            "client_id": self.config['mqtt']['client_id'],
            "status": "offline",
            "reason": "unexpected_disconnect",
            "timestamp": time.time()
        })
        
        topic = self.config['topics']['publish'].get('last_will', 
                   f"kitchen/device/{self.config['mqtt']['client_id']}/last_will")
        
        self.client.will_set(
            topic,
            payload=last_will_msg,
            qos=self.config['mqtt']['qos'],
            retain=True
        )
        
        print(f"📝 Last Will настроен на топик: {topic}")
    
    def get_timestamp(self):
        """Получение текущего времени в читаемом формате"""
        return datetime.now().strftime("%H:%M:%S")
    
    def connect(self):
        """Подключение к MQTT брокеру"""
        try:
            # Создаем клиента
            self.client = mqtt.Client(
                client_id=self.config['mqtt']['client_id'],
                protocol=mqtt.MQTTv311
            )
            
            # Настраиваем аутентификацию
            self.client.username_pw_set(
                self.config['mqtt']['username'],
                self.config['mqtt']['password']
            )
            
            # Настраиваем TLS
            if self.config['mqtt'].get('tls_enabled', False):
                self.client.tls_set(
                    cert_reqs=ssl.CERT_NONE if self.config['mqtt'].get('tls_insecure') else ssl.CERT_REQUIRED
                )
                print("🔒 TLS подключение настроено")
            
            # Настраиваем Last Will
            self.setup_last_will()
            
            # Устанавливаем callback функции
            self.client.on_connect = self.on_connect
            self.client.on_message = self.on_message
            self.client.on_disconnect = self.on_disconnect
            self.client.on_publish = self.on_publish
            
            # Подключаемся
            print(f"🔗 [{self.get_timestamp()}] Подключение к {self.config['mqtt']['broker']}...")
            self.client.connect(
                self.config['mqtt']['broker'],
                self.config['mqtt']['port'],
                self.config['mqtt']['keepalive']
            )
            
            # Запускаем сетевой цикл
            self.client.loop_start()
            
            # Ждем подключения
            timeout = 10
            start = time.time()
            while not self.connected and time.time() - start < timeout:
                time.sleep(0.5)
            
            if not self.connected:
                print(" Не удалось подключиться за отведенное время")
                return False
                
            return True
            
        except Exception as e:
            print(f" Ошибка подключения: {e}")
            return False
    
    def run(self):
        """Основной цикл работы"""
        try:
            # Подключаемся
            if not self.connect():
                return
            
            print("\n" + "=" * 50)
            print("Система запущена. Ожидание команд...")
            print("Нажмите Ctrl+C для остановки")
            print("=" * 50 + "\n")
            
            # Основной цикл
            while True:
                # Каждые 30 секунд отправляем heartbeat
                if int(time.time()) % 30 == 0:
                    if self.connected:
                        self.publish_status("online", "heartbeat")
                
                time.sleep(1)
                
        except KeyboardInterrupt:
            print(f"\n\n [{self.get_timestamp()}] Получен сигнал остановки")
        except Exception as e:
            print(f"\n Ошибка: {e}")
        finally:
            self.cleanup()
    
    def cleanup(self):
        """Корректное завершение работы"""
        print(f"\n[{self.get_timestamp()}] Завершение работы...")
        
        if self.connected:
            # Отправляем статус offline
            self.publish_status("offline", "shutdown")
            time.sleep(1)  # Даем время на отправку
        
        if self.client:
            self.client.loop_stop()
            self.client.disconnect()
        
        print(f"[{self.get_timestamp()}] Клиент остановлен. До свидания!")
        print("=" * 50)


def main():
    """Точка входа в программу"""
    client = RaspberryMQTTClient()
    client.run()


if __name__ == "__main__":
    main()