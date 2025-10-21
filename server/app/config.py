import os
from datetime import timedelta
from dotenv import load_dotenv

load_dotenv()

# Database
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./iot_server.db")

# JWT
JWT_SECRET = os.getenv("JWT_SECRET", "supersecretchangeit")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "60"))

# MQTT
MQTT_BROKER = os.getenv("MQTT_BROKER", "broker.hivemq.com")
MQTT_PORT = int(os.getenv("MQTT_PORT", "1883"))
MQTT_BASE_TOPIC = os.getenv("MQTT_BASE_TOPIC", "smart_home/demo")

# Server
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "8000"))

# Other
LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")

# 🔧 Настройки Firebase Push уведомлений
FCM_SERVER_KEY = os.getenv("FCM_SERVER_KEY", "YOUR_FIREBASE_SERVER_KEY_HERE")