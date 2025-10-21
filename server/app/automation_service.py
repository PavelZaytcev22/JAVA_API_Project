import json
import logging
from apscheduler.schedulers.background import BackgroundScheduler
from .crud import get_all_automations, get_device, update_device_state
from .database import SessionLocal
from .notifications import send_push_to_user

logger = logging.getLogger("automation")

_scheduler = BackgroundScheduler()
_scheduler.start()

# Called by mqtt_service.on_message after device state updated
def notify_mqtt_event(db, device_id: int, state: str):
    # Check automations with trigger_type == 'device_state'
    autos = db.query.self.query  # avoid mypy; but better to use get_all_automations
    # Simpler: fetch all and filter
    autos = get_all_automations(db)
    for a in autos:
        if not a.enabled or a.trigger_type != "device_state":
            continue
        # trigger_value is expected to be JSON string like {"device_id":123,"state":"ON"}
        try:
            tv = json.loads(a.trigger_value or "{}")
            if int(tv.get("device_id")) == device_id and str(tv.get("state")) == str(state):
                logger.info("Automation matched: %s -> executing", a.name)
                execute_action(a, db)
        except Exception:
            logger.exception("Failed parse trigger_value for automation %s", a.id)

def execute_action(automation, db):
    try:
        action = json.loads(automation.action)
        target_id = int(action.get("device_id"))
        new_state = str(action.get("state"))
    except Exception:
        logger.exception("Invalid action JSON for automation %s", automation.id)
        return
    device = get_device(db, target_id)
    if not device:
        logger.warning("Automation action target not found: %s", target_id)
        return
    update_device_state(db, device, new_state)
    # publish via MQTT so Pi receives (import here to avoid cycle)
    from .mqtt_service import publish_device_state
    publish_device_state(target_id, new_state)
    # send push to owner(s) of the home (simple approach: find home's owner)
    try:
        owner_id = device.home.owner_id
        send_push_to_user(db, owner_id, f"Automation: {automation.name}", f"Action executed on {device.name}: {new_state}", data={"automation_id": automation.id})
    except Exception:
        logger.exception("Failed to send push after automation")

# schedule automations with trigger_type == 'time'
def load_scheduled_automations():
    db = SessionLocal()
    try:
        autos = get_all_automations(db)
        for a in autos:
            if not a.enabled:
                continue
            if a.trigger_type == "time" and a.schedule:
                # schedule is a cron expression like "*/5 * * * *" or simpler: "interval:60" seconds
                if a.schedule.startswith("interval:"):
                    seconds = int(a.schedule.split(":",1)[1])
                    _scheduler.add_job(run_automation_job, "interval", seconds=seconds, args=[a.id], id=f"auto_{a.id}", replace_existing=True)
                    logger.info("Scheduled automation %s every %s seconds", a.name, seconds)
                else:
                    # unsupported cron parsing in this minimal impl
                    logger.warning("Unsupported schedule format for automation %s: %s", a.id, a.schedule)
    finally:
        db.close()

def run_automation_job(automation_id: int):
    db = SessionLocal()
    try:
        a = get_all_automations(db)
        # find by id
        a_obj = None
        for x in a:
            if x.id == automation_id:
                a_obj = x
                break
        if a_obj and a_obj.enabled:
            execute_action(a_obj, db)
    finally:
        db.close()