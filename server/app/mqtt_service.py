import paho.mqtt.client as mqtt
from paho.mqtt.enums import CallbackAPIVersion
import logging
from .config import MQTT_BROKER, MQTT_PORT, MQTT_BASE_TOPIC
from .crud import get_device, update_device_state
from .database import SessionLocal
import json

logger = logging.getLogger("mqtt")

MQTT_CLIENT = mqtt.Client(
    client_id="smart_home_server",
    callback_api_version=CallbackAPIVersion.VERSION2
)

def on_connect(client, userdata, flags, reasonCode, properties):
    logger.info("MQTT connected with rc=%s", rc)
    # subscribe to base topic
    client.subscribe(f"{MQTT_BASE_TOPIC}/#")
    logger.info("Subscribed to %s/#", MQTT_BASE_TOPIC)

def on_message(client, userdata, msg):
    try:
        payload = msg.payload.decode()
        topic = msg.topic
        logger.info("MQTT message received: %s -> %s", topic, payload)
        # Expected topic format: <base>/device/<id>/state  OR  <base>/device/<id>/cmd
        parts = topic.split("/")
        # Basic handling example
        if "device" in parts:
            try:
                idx = parts.index("device")
                device_id = int(parts[idx + 1])
            except Exception:
                logger.debug("Cannot parse device id from topic: %s", topic)
                return
            # payload may be JSON or simple string
            new_state = payload
            # Update DB
            db = SessionLocal()
            try:
                device = get_device(db, device_id)
                if device:
                    update_device_state(db, device, new_state)
                    logger.info("Device %s state updated in DB to %s", device_id, new_state)
                else:
                    logger.warning("Received MQTT for unknown device id=%s", device_id)
            finally:
                db.close()
    except Exception as e:
        logger.exception("Error processing MQTT message: %s", e)

MQTT_CLIENT.on_connect = on_connect
MQTT_CLIENT.on_message = on_message

def start_mqtt():
    try:
        MQTT_CLIENT.connect(MQTT_BROKER, MQTT_PORT, keepalive=60)
        MQTT_CLIENT.loop_start()
        logger.info("MQTT loop started")
    except Exception as e:
        logger.exception("Failed to start MQTT client: %s", e)

def publish_device_state(device_id: int, state: str):
    topic = f"{MQTT_BASE_TOPIC}/device/{device_id}/state"
    payload = state
    try:
        MQTT_CLIENT.publish(topic, payload)
        logger.info("Published to %s : %s", topic, payload)
    except Exception:
        logger.exception("Failed to publish MQTT message")
