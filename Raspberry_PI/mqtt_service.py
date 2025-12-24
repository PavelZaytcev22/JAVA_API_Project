import paho.mqtt.client as mqtt
import ssl
import json
import time
import yaml
import random
import signal
import sys
from datetime import datetime

# Глобальные переменные
running = True
connected = False

def load_config(config_file="config.yaml"):
    """Загрузка конфигурации"""
    try:
        with open(config_file, 'r') as f:
            config = yaml.safe_load(f)
        print(f"Конфигурация загружена")
        return config
    except Exception as e:
        print(f"шибка загрузки конфигурации: {e}")
        sys.exit(1)

def signal_handler(sig, frame):
    """Обработка Ctrl+C"""
    global running
    print("\nПолучен сигнал остановки")
    running = False

def on_connect(client, userdata, flags, rc):
    """Обработка подключения к брокеру"""
    global connected
    config = userdata['config']
    
    if rc == 0:
        connected = True
        current_time = datetime.now().strftime("%H:%M:%S")
        print(f"[{current_time}] Успешное подключение!")
        
        # 1. ПОДПИСКА НА КОМАНДЫ
        command_topic = config['publish']['command_topic']
        client.subscribe(command_topic, qos=1)
        print(f"   📫 Подписан на: {command_topic}")
        
        # 2. ПУБЛИКАЦИЯ СТАТУСА ONLINE
        publish_status(client, config, "online", retain=True)
        
    else:
        error_msgs = {
            1: "Неверная версия протокола",
            2: "Неверный client_id",
            3: "Сервер недоступен",
            4: "Неверные логин/пароль",
            5: "Нет прав на подключение"
        }
        print(f"Ошибка подключения: {error_msgs.get(rc, f'Код {rc}')}")

def on_message(client, userdata, msg):
    """Обработка входящих сообщений"""
    current_time = datetime.now().strftime("%H:%M:%S")
    print(f"📨 [{current_time}] Команда получена:")
    print(f"   Топик: {msg.topic}")
    print(f"   Данные: {msg.payload.decode()}")
    
    # Здесь можно добавить обработку команд
    # Например: если msg.payload == "turn_on_light", то включить свет через GPIO

def on_disconnect(client, userdata, rc):
    """Обработка отключения"""
    global connected
    connected = False
    current_time = datetime.now().strftime("%H:%M:%S")
    
    if rc != 0:
        print(f"⚠️ [{current_time}] Неожиданное отключение")

def publish_status(client, config, status, retain=False):
    """Публикация статуса устройства"""
    if not connected:
        return
    
    topic = config['publish']['status_topic']
    client.publish(
        topic,
        payload=status,
        qos=1,
        retain=retain
    )
    
    current_time = datetime.now().strftime("%H:%M:%S")
    print(f"📤 [{current_time}] Статус '{status}' отправлен")

def read_sensor_data():
    """
    Чтение данных с датчиков
    В реальном проекте замените на чтение GPIO
    """
    return {
        'temperature': round(random.uniform(20.0, 25.0), 1),
        'humidity': round(random.uniform(40.0, 60.0), 1),
        'motion': random.choice([True, False])
    }

def publish_sensor_data(client, config):
    """Публикация данных датчиков в формате JSON"""
    if not connected:
        return
    
    sensor_data = read_sensor_data()
    current_time = datetime.now().isoformat()
    
    # Температура
    temperature_json = {
        "value": sensor_data['temperature'],
        "timestamp": current_time,
        "unit": "°C"
    }
    client.publish(
        config['publish']['temperature_topic'],
        payload=json.dumps(temperature_json),
        qos=1
    )
    
    # Влажность
    humidity_json = {
        "value": sensor_data['humidity'],
        "timestamp": current_time,
        "unit": "%"
    }
    client.publish(
        config['publish']['humidity_topic'],
        payload=json.dumps(humidity_json),
        qos=1
    )
    
    # Движение
    motion_json = {
        "value": sensor_data['motion'],
        "timestamp": current_time,
        "unit": "boolean"
    }
    client.publish(
        config['publish']['motion_topic'],
        payload=json.dumps(motion_json),
        qos=1
    )
    
    # Вывод в консоль
    print(f"📊 [{datetime.now().strftime('%H:%M:%S')}] Данные отправлены:")
    print(f"   🌡️  {sensor_data['temperature']}°C")
    print(f"   💧 {sensor_data['humidity']}%")
    print(f"   🏃 {'ЕСТЬ' if sensor_data['motion'] else 'НЕТ'} движения")

def setup_mqtt_client(config):
    """Настройка и создание MQTT клиента"""
    print("=" * 50)
    print("MQTT КЛИЕНТ ДЛЯ RASPBERRY PI")
    print("=" * 50)
    print(f"Брокер: {config['mqtt']['broker']}:{config['mqtt']['port']}")
    print(f"Client ID: {config['mqtt']['client_id']}")
    print(f"Пользователь: {config['mqtt']['userName']}")
    print("=" * 50)
    
    # Создаем клиента
    client = mqtt.Client(
        client_id=config['mqtt']['client_id'],
        userdata={'config': config}
    )
    
    # Аутентификация
    client.username_pw_set(
        config['mqtt']['userName'],
        config['mqtt']['password']
    )
    
    # Настройка TLS
    if config['mqtt'].get('tls_enabled', False):
        client.tls_set(cert_reqs=ssl.CERT_NONE)
        print("🔒 TLS подключение настроено")
    
    # Callback функции
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_disconnect = on_disconnect
    
    # Настройка Last Will (сообщение при отключении)
    client.will_set(
        config['publish']['status_topic'],
        payload="offline",
        qos=1,
        retain=True
    )
    print("📝 Last Will настроен (offline при отключении)")
    
    return client

def main():
    """Основная функция"""
    global running, connected
    
    # Обработка Ctrl+C
    signal.signal(signal.SIGINT, signal_handler)
    
    # Загрузка конфигурации
    config = load_config()
    
    # Настройка MQTT клиента
    client = setup_mqtt_client(config)
    
    try:
        # Подключение к брокеру
        print(f"🔗 Подключаюсь к {config['mqtt']['broker']}...")
        client.connect(
            config['mqtt']['broker'],
            config['mqtt']['port'],
            config['mqtt']['keepalive']
        )
        
        # Запуск сетевого цикла
        client.loop_start()
        
        # Ждем подключения
        print("⏳ Ожидание подключения...")
        for _ in range(20):
            if connected:
                break
            time.sleep(0.5)
        
        if not connected:
            print("Не удалось подключиться")
            return
        
        print("\n" + "=" * 50)
        print("СИСТЕМА ЗАПУЩЕНА")
        print("=" * 50)
        print(f"📡 Отправка данных каждые {config['publish']['sensor_interval']} сек")
        print("\nНажмите Ctrl+C для остановки")
        print("=" * 50 + "\n")
        
        # Основной цикл работы
        last_publish_time = 0
        interval = config['publish']['sensor_interval']
        
        while running:
            current_time = time.time()
            
            # Регулярная публикация данных датчиков
            if current_time - last_publish_time >= interval:
                publish_sensor_data(client, config)
                last_publish_time = current_time
            
            time.sleep(0.1)
            
    except Exception as e:
        print(f"\nОшибка: {e}")
    finally:
        # Корректное завершение
        print("\n" + "=" * 50)
        print("ЗАВЕРШЕНИЕ РАБОТЫ...")
        print("=" * 50)
        
        # Публикация статуса offline
        if connected:
            publish_status(client, config, "offline", retain=True)
            time.sleep(1)
        
        # Остановка клиента
        client.loop_stop()
        client.disconnect()
        
        print("Клиент остановлен")
        print("=" * 50)

if __name__ == "__main__":
    main()