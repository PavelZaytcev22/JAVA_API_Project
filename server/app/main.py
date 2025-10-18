from fastapi import FastAPI
from contextlib import asynccontextmanager
from .database import Base, engine
from .routes import auth as auth_router, devices as devices_router, system as system_router
from .mqtt_service import start_mqtt
from .utils import setup_logging
import logging

setup_logging()
logger = logging.getLogger("app")

# Create DB tables if not exist
Base.metadata.create_all(bind=engine)

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting MQTT service...")
    try:
        start_mqtt()
        logger.info("MQTT service started.")
    except Exception as e:
        logger.exception("Failed starting MQTT: %s", e)
    yield
    logger.info("Shutting down app...")

app = FastAPI(title="Smart Home Server", version="1.0", lifespan=lifespan)

# include routers
app.include_router(auth_router.router)
app.include_router(devices_router.router)
app.include_router(system_router.router)

@app.get("/")
def root():
    return {"message": "Welcome to Smart Home Server"}
