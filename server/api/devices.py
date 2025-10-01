from flask import request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models.database import db
from models.user import User
from models.device import Device

# DeviceManager будет передан из app.py
device_manager = None

def init_devices_routes(app, manager):
    """Инициализация маршрутов устройств"""
    global device_manager
    device_manager = manager
    
    @app.route('/api/devices', methods=['GET'])
    @jwt_required()
    def get_devices():
        """Получение списка всех устройств"""
        user_id = get_jwt_identity()
        
        devices = Device.query.all()
        devices_data = [device.to_dict() for device in devices]
        
        return jsonify({
            "status": "success",
            "devices": devices_data
        })

    @app.route('/api/devices/<int:device_id>/control', methods=['POST'])
    @jwt_required()
    def control_device(device_id):
        """Управление устройством"""
        user_id = get_jwt_identity()
        
        if not request.is_json:
            return jsonify({"msg": "Missing JSON in request"}), 400

        data = request.get_json()
        action = data.get('action')
        
        if not action or action not in ['on', 'off', 'toggle']:
            return jsonify({"msg": "Invalid action"}), 400

        device = Device.query.get(device_id)
        if not device:
            return jsonify({"msg": "Device not found"}), 404

        # Получаем имя устройства для менеджера
        result = device_manager.control_device(device.name, action, user_id)
        
        if "error" in result:
            return jsonify({"status": "error", "message": result["error"]}), 400
        
        return jsonify({
            "status": "success",
            "device": device.name,
            "action": action,
            "result": result
        })

    @app.route('/api/devices/<int:device_id>/read', methods=['GET'])
    @jwt_required()
    def read_device(device_id):
        """Чтение данных с устройства"""
        user_id = get_jwt_identity()
        
        device = Device.query.get(device_id)
        if not device:
            return jsonify({"msg": "Device not found"}), 404

        result = device_manager.read_sensor(device.name, user_id)
        
        if "error" in result:
            return jsonify({"status": "error", "message": result["error"]}), 400
        
        return jsonify({
            "status": "success",
            "device": device.name,
            "data": result
        })