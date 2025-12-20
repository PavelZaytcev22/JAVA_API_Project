# ultra_simple.py
import yaml
import time
import RPi.GPIO as GPIO
import Adafruit_DHT

# Загрузка конфигурации
with open('config.yaml', 'r') as f:
    config = yaml.safe_load(f)

# Настройка GPIO
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

# Получаем конфигурации устройств
light_config = config['devices']['living_room_light']
sensor_config = config['devices']['temperature_sensor']

# Настраиваем свет
LIGHT_PIN = light_config['pin']
GPIO.setup(LIGHT_PIN, GPIO.OUT)
light_state = False  # False = выкл, True = вкл

# Пин датчика
SENSOR_PIN = sensor_config['pin']

def light_on():
    """Включить свет"""
    GPIO.output(LIGHT_PIN, GPIO.HIGH)
    global light_state
    light_state = True
    print("💡 Свет ВКЛЮЧЕН")

def light_off():
    """Выключить свет"""
    GPIO.output(LIGHT_PIN, GPIO.LOW)
    global light_state
    light_state = False
    print("💡 Свет ВЫКЛЮЧЕН")

def read_temperature():
    """Прочитать температуру"""
    try:
        humidity, temperature = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, SENSOR_PIN)
        if temperature is not None:
            print(f"🌡️ Температура: {temperature:.1f}°C")
            return temperature
        else:
            print("Ошибка чтения датчика")
            return None
    except Exception as e:
        print(f"Ошибка: {e}")
        return None

# Простой интерфейс
print("=== Управление светом и температурой ===")
print("Устройства из config.yaml:")
print(f"1. {light_config['name']} (пин: {LIGHT_PIN})")
print(f"2. {sensor_config['name']} (пин: {SENSOR_PIN})")
print()

try:
    while True:
        print("\nЧто сделать?")
        print("1. Включить свет")
        print("2. Выключить свет")
        print("3. Проверить температуру")
        print("4. Автоуправление светом по температуре")
        print("q. Выйти")
        
        choice = input("Выберите: ").strip()
        
        if choice == '1':
            light_on()
            
        elif choice == '2':
            light_off()
            
        elif choice == '3':
            temp = read_temperature()
            
        elif choice == '4':
            print("\n🔧 Автоматический режим")
            print("Свет включится при t < 20°C, выключится при t > 22°C")
            print("Нажмите Ctrl+C для остановки")
            
            try:
                while True:
                    temp = read_temperature()
                    if temp:
                        if temp < 20 and not light_state:
                            light_on()
                        elif temp > 22 and light_state:
                            light_off()
                    
                    time.sleep(5)  # Проверка каждые 5 секунд
                    
            except KeyboardInterrupt:
                print("\nАвторежим остановлен")
                
        elif choice.lower() == 'q':
            print("Выход...")
            break
            
        else:
            print("Неизвестная команда")

except KeyboardInterrupt:
    print("\nПрограмма завершена")

finally:
    # Всегда выключаем свет и чистим GPIO
    light_off()
    GPIO.cleanup()
    print("🧹 Ресурсы освобождены")