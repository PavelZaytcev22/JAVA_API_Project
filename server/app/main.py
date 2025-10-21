from fastapi import FastAPI
from contextlib import asynccontextmanager
from .database import Base, engine
from .routes import auth as auth_router, devices as devices_router, rooms as rooms_router, automations as autos_router, system as system_router
from .mqtt_service import start_mqtt
from .utils import setup_logging
from .automation_service import load_scheduled_automations
import logging

setup_logging()
logger = logging.getLogger("app")

Base.metadata.create_all(bind=engine)

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting MQTT client...")
    start_mqtt()
    # load scheduled automations into APScheduler
    try:
        load_scheduled_automations()
    except Exception:
        logger.exception("Failed to load scheduled automations")
    yield
    logger.info("Shutting down")

app = FastAPI(title="Smart Home Server", version="1.0", lifespan=lifespan)

app.include_router(auth_router.router)
app.include_router(devices_router.router)
app.include_router(rooms_router.router)
app.include_router(autos_router.router)
app.include_router(system_router.router)

@app.get("/")
def root():
    return {"message":"Welcome to Smart Home Server"}