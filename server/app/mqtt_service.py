import paho.mqtt.client as mqtt
from paho.mqtt.enums import CallbackAPIVersion
import logging
from .config import MQTT_BROKER, MQTT_PORT, MQTT_BASE_TOPIC
from .database import SessionLocal
from .crud import get_device, update_device_state, add_sensor_history
from .automation_service import notify_mqtt_event  # will be used to trigger automations
import json

logger = logging.getLogger("mqtt")

MQTT_CLIENT = mqtt.Client(client_id="smart_home_server", callback_api_version=CallbackAPIVersion.VERSION2)

def on_connect(client, userdata, flags, rc, properties=None):
    logger.info("MQTT connected rc=%s", rc)
    topic = f"{MQTT_BASE_TOPIC}/#"
    client.subscribe(topic)
    logger.info("Subscribed to %s", topic)

def on_message(client, userdata, msg):
    try:
        topic = msg.topic
        payload = msg.payload.decode()
        logger.info("MQTT recv: %s -> %s", topic, payload)
        # parse topics like base/device/{id}/state or base/device/{id}/cmd
        parts = topic.split("/")
        # find 'device' part
        if "device" in parts:
            idx = parts.index("device")
            try:
                device_id = int(parts[idx + 1])
            except Exception:
                logger.debug("invalid device id in topic %s", topic)
                return
            # update DB state
            db = SessionLocal()
            try:
                device = get_device(db, device_id)
                if device:
                    # Save history
                    add_sensor_history(db, device_id, payload)
                    # Update current state
                    update_device_state(db, device, payload)
                    logger.info("Device %s state -> %s", device_id, payload)
                    # Trigger automations that listen to MQTT events
                    notify_mqtt_event(db, device_id, payload)
                else:
                    logger.warning("Unknown device id %s", device_id)
            finally:
                db.close()
    except Exception:
        logger.exception("Error in on_message")

def start_mqtt():
    try:
        MQTT_CLIENT.on_connect = on_connect
        MQTT_CLIENT.on_message = on_message
        MQTT_CLIENT.connect(MQTT_BROKER, MQTT_PORT, keepalive=60)
        MQTT_CLIENT.loop_start()
        logger.info("MQTT loop started")
    except Exception:
        logger.exception("Failed to start MQTT")

def publish_device_state(device_id: int, state: str):
    topic = f"{MQTT_BASE_TOPIC}/device/{device_id}/cmd"
    try:
        MQTT_CLIENT.publish(topic, state)
        logger.info("Published %s -> %s", topic, state)
    except Exception:
        logger.exception("Failed publish")