from .database import db

class Device(db.Model):
    __tablename__ = 'devices'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    device_type = db.Column(db.String(50), nullable=False)
    device_subtype = db.Column(db.String(50))
    pin = db.Column(db.Integer)
    topic = db.Column(db.String(200))
    ip_address = db.Column(db.String(50))
    state = db.Column(db.String(20), default='off')
    description = db.Column(db.Text)
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'type': self.device_type,
            'subtype': self.device_subtype,
            'pin': self.pin,
            'topic': self.topic,
            'ip_address': self.ip_address,
            'state': self.state,
            'description': self.description
        }