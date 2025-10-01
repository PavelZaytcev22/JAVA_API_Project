from flask import request, jsonify
from flask_jwt_extended import create_access_token
from models.database import db
from models.user import User
from models.log import Log

def init_auth_routes(app):
    """Инициализация маршрутов аутентификации"""
    
    @app.route('/api/login', methods=['POST'])
    def login():
        """Аутентификация пользователя"""
        if not request.is_json:
            return jsonify({"msg": "Missing JSON in request"}), 400

        username = request.json.get('username')
        password = request.json.get('password')

        if not username or not password:
            return jsonify({"msg": "Missing username or password"}), 400

        user = User.query.filter_by(username=username).first()
        
        if user and user.check_password(password):
            # Логирование успешного входа
            log = Log(
                user_id=user.id,
                action="login",
                result="success"
            )
            db.session.add(log)
            db.session.commit()
            
            access_token = create_access_token(identity=user.id)
            return jsonify({
                "access_token": access_token,
                "user": user.to_dict()
            })
        
        # Логирование неудачной попытки
        log = Log(
            action="login_failed",
            result="error",
            details=f"Failed login attempt for user: {username}"
        )
        db.session.add(log)
        db.session.commit()
        
        return jsonify({"msg": "Bad username or password"}), 401

    @app.route('/api/register', methods=['POST'])
    def register():
        """Регистрация нового пользователя"""
        if not request.is_json:
            return jsonify({"msg": "Missing JSON in request"}), 400

        username = request.json.get('username')
        password = request.json.get('password')
        role = request.json.get('role', 'user')

        if not username or not password:
            return jsonify({"msg": "Missing username or password"}), 400

        if User.query.filter_by(username=username).first():
            return jsonify({"msg": "Username already exists"}), 400

        user = User(username=username, role=role)
        user.set_password(password)
        
        db.session.add(user)
        db.session.commit()

        # Логирование регистрации
        log = Log(
            user_id=user.id,
            action="register",
            result="success"
        )
        db.session.add(log)
        db.session.commit()

        return jsonify({
            "msg": "User created successfully",
            "user": user.to_dict()
        }), 201