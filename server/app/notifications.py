import requests
import logging
from .config import FCM_SERVER_KEY

logger = logging.getLogger("notifications")

FCM_URL = "https://fcm.googleapis.com/fcm/send"

def send_push_to_token(token: str, title: str, body: str, data: dict = None):
    if not FCM_SERVER_KEY:
        logger.info("FCM_SERVER_KEY not set — would send push to %s: %s - %s", token, title, body)
        return False
    headers = {
        "Authorization": f"key={FCM_SERVER_KEY}",
        "Content-Type": "application/json"
    }
    payload = {
        "to": token,
        "notification": {"title": title, "body": body},
        "data": data or {}
    }
    try:
        r = requests.post(FCM_URL, headers=headers, json=payload, timeout=5)
        r.raise_for_status()
        logger.info("Push sent to token %s", token)
        return True
    except Exception as e:
        logger.exception("Failed to send push: %s", e)
        return False

def send_push_to_user(db, user_id: int, title: str, body: str, data: dict = None):
    from .crud import get_push_tokens_for_user
    tokens = get_push_tokens_for_user(db, user_id)
    results = []
    for t in tokens:
        results.append(send_push_to_token(t.token, title, body, data))
    return results