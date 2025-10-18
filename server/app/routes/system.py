from fastapi import APIRouter, Depends
from .. import auth
from ..database import SessionLocal

router = APIRouter(prefix="/api/system", tags=["system"])

@router.get("/ping")
def ping():
    return {"status": "ok", "message": "Server is running"}

@router.get("/me")
def me(current_user = Depends(auth.get_current_user)):
    return {"username": current_user.username, "full_name": current_user.full_name}
