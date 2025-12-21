import requests
import logging
from .config import FCM_SERVER_KEY

# Настройка логирования для модуля уведомлений
logger = logging.getLogger("notifications")

# URL FCM (Firebase Cloud Messaging) API
FCM_URL = "https://fcm.googleapis.com/fcm/send"


def send_push_to_token(token: str, title: str, body: str, data: dict = None):
    """
    Отправляет push-уведомление на конкретный FCM токен
    
    Args:
        token: FCM токен устройства
        title: заголовок уведомления
        body: текст уведомления
        data: дополнительные данные для уведомления (опционально)
    
    Returns:
        bool: True если отправка успешна, False в случае ошибки
    """
    # Проверяем наличие FCM серверного ключа
    if not FCM_SERVER_KEY:
        logger.info(
            "FCM_SERVER_KEY не настроен — пропуск отправки уведомления: %s - %s", 
            title, body
        )
        return False
    
    # Заголовки для FCM API
    headers = {
        "Authorization": f"key={FCM_SERVER_KEY}",
        "Content-Type": "application/json"
    }
    
    # Структура payload для FCM
    payload = {
        "to": token,
        "notification": {
            "title": title, 
            "body": body
        },
        "data": data or {}  # дополнительные данные для приложения
    }
    
    try:
        # Отправка запроса к FCM API
        response = requests.post(FCM_URL, headers=headers, json=payload, timeout=5)
        response.raise_for_status()  # Проверка HTTP статуса
        
        logger.info("Уведомление успешно отправлено на токен: %s", token)
        return True
        
    except requests.exceptions.RequestException as e:
        # Логируем ошибки сети или HTTP
        logger.error("Ошибка отправки уведомления на токен %s: %s", token, e)
        return False
    except Exception as e:
        # Логируем все остальные ошибки
        logger.exception("Неожиданная ошибка при отправке уведомления: %s", e)
        return False


def send_push_to_user(db, user_id: int, title: str, body: str, data: dict = None):
    """
    Отправляет push-уведомление всем устройствам пользователя
    
    Args:
        db: сессия базы данных
        user_id: ID пользователя
        title: заголовок уведомления
        body: текст уведомления
        data: дополнительные данные (опционально)
    
    Returns:
        list: список результатов отправки для каждого токена пользователя
    """
    from .crud import get_push_tokens_for_user
    
    # Получаем все FCM токены пользователя
    user_tokens = get_push_tokens_for_user(db, user_id)
    
    if not user_tokens:
        logger.info("У пользователя %s нет зарегистрированных FCM токенов", user_id)
        return []
    
    logger.info("Отправка уведомления пользователю %s на %d устройств", user_id, len(user_tokens))
    
    # Отправляем уведомление на каждый токен пользователя
    results = []
    for token_record in user_tokens:
        result = send_push_to_token(
            token=token_record.token, 
            title=title, 
            body=body, 
            data=data
        )
        results.append(result)
    
    # Логируем общую статистику отправки
    successful_sends = sum(results)
    logger.info(
        "Статистика отправки для пользователя %s: %d/%d успешно",
        user_id, successful_sends, len(results)
    )
    
    return results