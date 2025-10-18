import logging
from .config import LOG_LEVEL

def setup_logging():
    level = getattr(logging, LOG_LEVEL.upper(), logging.INFO)
    logging.basicConfig(
        level=level,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s"
    )
    # reduce noisy logs from libraries if desired
    logging.getLogger("sqlalchemy.engine").setLevel(logging.WARNING)
    logging.getLogger("mqtt").setLevel(logging.INFO)
