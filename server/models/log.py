from .database import db
from datetime import datetime

class Log(db.Model):
    __tablename__ = 'logs'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    action = db.Column(db.String(100), nullable=False)
    device_id = db.Column(db.Integer, db.ForeignKey('devices.id'))
    result = db.Column(db.String(20))
    details = db.Column(db.Text)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    
    user = db.relationship('User', backref=db.backref('logs', lazy=True))
    device = db.relationship('Device', backref=db.backref('logs', lazy=True))
    
    def to_dict(self):
        return {
            'id': self.id,
            'user': self.user.username if self.user else None,
            'action': self.action,
            'device': self.device.name if self.device else None,
            'result': self.result,
            'details': self.details,
            'timestamp': self.timestamp.isoformat()
        }