# 🏠 Smart Home Control System

## 📖 Описание

Система управления умным домом, состоящая из **облачного сервера (FastAPI)**, **Raspberry Pi-шлюза** и **мобильного Android-приложения**.  
Проект позволяет централизованно управлять умными устройствами, выполнять сценарии автоматизации и получать уведомления в реальном времени.

---

## 🎯 Основные функции

- Управление устройствами в доме (через Raspberry Pi)
- Хранение конфигурации домов, комнат и устройств
- Создание сценариев и автоматических правил
- Push-уведомления о событиях
- Авторизация пользователей через JWT
- Работа с MQTT для обмена сообщениями между сервером и Raspberry

---

## 🧩 Архитектура системы

📱 Android App
     │  (HTTP / HTTPS)
     ▼
🌐 FastAPI Server
 ├── Auth (JWT)
 ├── REST API
 ├── MQTT client
 ├── Automation Engine
 ├── SQLite Database
 └── Push Notifications (FCM)
     │
     ▼
🍓 Raspberry Pi
 ├── Device Manager
 ├── State Storage (YAML)
 ├── Automation triggers
 └── MQTT Publisher/Subscriber

---

## ⚙️ Подсистемы

| Подсистема | Назначение |
|-------------|------------|
| **FastAPI** | Основное приложение, REST API, авторизация |
| **MQTT Service** | Связь между сервером и Raspberry Pi |
| **Automation Engine** | Выполнение автоматизаций и сценариев |
| **Notifications** | Push-уведомления пользователям через FCM |
| **SQLite (SQLAlchemy)** | Лёгкая встроенная база данных |
| **Raspberry Pi Client** | Управление физическими устройствами |
| **Android Client** | Интерфейс управления домом |

---

## 🧰 Технологический стек

**Backend (Server):**
- Python 3.13
- FastAPI
- SQLite + SQLAlchemy ORM
- paho-mqtt
- APScheduler
- Firebase Cloud Messaging (FCM)

**IoT (Raspberry Pi):**
- Python + paho-mqtt + yaml + gpiozero
- Хранение локальных состояний устройств в YAML

**Mobile (Android):**
- Java (Android SDK)
- Retrofit2 + Gson для REST API
- Firebase SDK для push-уведомлений
- Material Design UI

---

## 🔗 Взаимодействие компонентов

| Компонент | Протокол | Назначение |
|------------|-----------|------------|
| **Android ↔ Server** | HTTP / HTTPS | Запросы REST API: вход, список устройств, действия |
| **Server ↔ Raspberry Pi** | MQTT | Передача команд и состояний устройств |
| **Server ↔ Android** | FCM | Push-уведомления о событиях и сценариях |
| **Raspberry Pi (локально)** | GPIO / Wi-Fi | Управление физическими устройствами |

---

## 🚀 Быстрый старт

### 1. Запуск сервера
```bash
uvicorn app.main:app --reload
```
Документация API доступна по адресу:
```
http://127.0.0.1:8000/docs
```

### 2. Подключение Raspberry Pi
1. Установите библиотеку:
   ```bash
   pip install paho-mqtt
   ```
2. Укажите параметры брокера MQTT и сервера в `config.yaml`
3. Запустите скрипт клиента для обмена сообщениями

### 3. Android клиент
- Использует `Retrofit2` для запросов:
  - `POST /api/login` — вход
  - `GET /api/devices` — список устройств
  - `POST /api/devices/{id}/action` — управление
- Хранит JWT-токен в `SharedPreferences`
- Получает push-уведомления через Firebase

---

## 🔮 Преимущества

- Лёгкий и быстрый сервер FastAPI  
- MQTT обеспечивает связь в реальном времени  
- Поддержка офлайн-режима Raspberry  
- Автоматизации и сценарии без ручного вмешательства  
- Push-уведомления пользователю при изменении состояния устройств  

---

## 📄 Команда проекта

**Smart Home Team**  
- 🧠 **Backend (FastAPI)** — Коняев Александр (alexkonru)
- 📱 **Android Client** — Казанцев Антон (tuchkaSoul)
- 🍓 **Raspberry Pi (IoT)** — Зайцев Павел (PavelZaytsev22)  

