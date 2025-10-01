import sys
import os

def setup_gpio():
    """Настройка GPIO (реальный или фейковый)"""
    try:
        import RPi.GPIO as GPIO
        IS_RASPBERRY_PI = True
        print("✓ Режим Raspberry Pi (реальные GPIO)")
    except (ImportError, RuntimeError):
        from fake_rpi import toggle_print
        toggle_print(False)
        sys.modules['RPi'] = __import__('fake_rpi').RPi
        sys.modules['RPi.GPIO'] = __import__('fake_rpi').RPi.GPIO
        import RPi.GPIO as GPIO
        IS_RASPBERRY_PI = False
        print("✓ Режим эмуляции (fake-rpi)")

    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    
    return GPIO, IS_RASPBERRY_PI