from flask import request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import User, Log
from . import api_bp

@api_bp.route('/logs', methods=['GET'])
@jwt_required()
def get_logs():
    """Получение логов (только для админов)"""
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    
    if user.role != 'admin':
        return jsonify({"msg": "Admin access required"}), 403

    # Пагинация
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 50, type=int)
    
    logs_query = Log.query.order_by(Log.timestamp.desc())
    pagination = logs_query.paginate(
        page=page, per_page=per_page, error_out=False
    )
    
    logs_data = [log.to_dict() for log in pagination.items]
    
    return jsonify({
        "status": "success",
        "logs": logs_data,
        "pagination": {
            "page": page,
            "per_page": per_page,
            "total": pagination.total,
            "pages": pagination.pages
        }
    })