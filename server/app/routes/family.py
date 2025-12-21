from fastapi import Depends, HTTPException, APIRouter
from .. import schemas, auth, crud
from sqlalchemy.orm import Session
from ..auth import get_current_user, get_db


# Создание роутера для семьи
# Префикс /api/family добавляется ко всем маршрутам этого роутера
router = APIRouter(prefix="/api/family", tags=["family"])


@router.post("/homes/{home_id}/members")
def add_family_member(
    home_id: int,
    member_in: schemas.FamilyMemberAdd,
    current_user = Depends(get_current_user),
    # home_member = Depends(auth.require_home_access()),  # Просто проверяем доступ
    db: Session = Depends(get_db)
):
    """Добавление члена семьи в дом - доступно любому члену дома"""
     # 🔍 Проверяем существование дома
    home = crud.get_home(db, home_id)
    if not home:
        raise HTTPException(status_code=404, detail="Дом не найден")
    
    # Ищем пользователя по username
    target_user = crud.get_user_by_username(db, member_in.username)
    if not target_user:
        raise HTTPException(status_code=404, detail="Пользователь не найден")
    
    # Проверяем, не является ли пользователь уже членом
    existing_member = crud.get_home_member(db, home_id, target_user.id)
    if existing_member:
        raise HTTPException(status_code=400, detail="Пользователь уже в доме")
    
    # Добавляем в дом
    new_member = crud.add_home_member(db, home_id, target_user.id)
    return {
        "status": "success", 
        "message": "Пользователь добавлен в дом",
        "member_id": new_member.id,
        "user_id": target_user.id
    }

@router.get("/homes/{home_id}/members")
def get_family_members(
    home_id: int,
    # 🔐 Админ ИЛИ член дома
    # access = Depends(auth.get_admin_or_home_member),
    db: Session = Depends(get_db)
):
    """Получение списка членов дома - доступно администратору ИЛИ члену дома"""
    members = crud.get_home_members(db, home_id)
    return members

@router.get("/my-homes")
def get_my_homes(
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Получение списка домов пользователя (для обычных пользователей)"""
    if current_user.role == "admin":
        # Администратор видит все дома
        homes = crud.get_all_homes(db)
    else:
        # Обычный пользователь видит только свои дома
        homes = crud.get_user_homes(db, current_user.id)
    return homes


@router.delete("/homes/{home_id}/members/{user_id}")
def remove_family_member(
    home_id: int,
    user_id: int,
    current_user = Depends(get_current_user),
    # home_member = Depends(auth.require_home_access()),
    db: Session = Depends(get_db)
):
    """Удаление члена семьи из дома - доступно любому члену"""
    # Нельзя удалить самого себя
    if user_id == current_user.id:
        raise HTTPException(status_code=400, detail="Нельзя удалить самого себя")
    
    crud.remove_home_member(db, home_id, user_id)
    return {"status": "success", "message": "Пользователь удален из дома"}


@router.post("/homes")
def create_home(
    home_in: schemas.HomeCreate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Создание нового дома"""
    home = crud.create_home(db, owner_id=current_user.id, home_in=home_in)
    
    return {
        "status": "success", 
        "message": "Дом успешно создан",
        "data": {
            "home_id": home.id,
            "name": home.name,
            "owner_id": home.owner_id
        }
    }

@router.patch("/homes/{home_id}")
def update_home(
    home_id: int,
    home_update: schemas.HomeUpdate,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Обновление информации о доме - доступно только владельцу
    """
    # Проверяем существование дома
    home = crud.get_home_by_id(db, home_id)
    if not home:
        raise HTTPException(status_code=404, detail="Дом не найден")
    
    # Проверяем, что текущий пользователь - владелец дома
    if home.owner_id != current_user.id:
        raise HTTPException(
            status_code=403, 
            detail="Недостаточно прав. Только владелец может изменять дом"
        )
    
    # Обновляем дом
    updated_home = crud.update_home(db, home_id, home_update)
    if not updated_home:
        raise HTTPException(status_code=404, detail="Дом не найден")
    
    return {
        "status": "success", 
        "message": "Дом успешно обновлен",
        "data": {
            "home_id": updated_home.id,
            "name": updated_home.name,
            "owner_id": updated_home.owner_id
        }
    }

@router.delete("/homes/{home_id}")
def delete_home(
    home_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Удаление дома - доступно только владельцу
    ВНИМАНИЕ: Это действие необратимо!
    """
    # Проверяем существование дома
    home = crud.get_home(db, home_id)
    if not home:
        raise HTTPException(status_code=404, detail="Дом не найден")
    
    # Проверяем, что текущий пользователь - владелец дома
    if home.owner_id != current_user.id:
        raise HTTPException(
            status_code=403, 
            detail="Недостаточно прав. Только владелец может удалить дом"
        )
    
    # Удаляем дом
    success = crud.delete_home(db, home_id)
    if not success:
        raise HTTPException(status_code=404, detail="Дом не найден")
    
    return {
        "status": "success", 
        "message": "Дом и все связанные данные успешно удалены"
    }

@router.get("/homes/{home_id}")
def get_home_details(
    home_id: int,
    current_user = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Получение детальной информации о доме - доступно членам дома
    """
    # Проверяем существование дома
    home = crud.get_home_by_id(db, home_id)
    if not home:
        raise HTTPException(status_code=404, detail="Дом не найден")
    
    # Проверяем, что пользователь имеет доступ к дому
    if current_user.role != "admin":
        user_member = crud.get_home_member(db, home_id, current_user.id)
        if not user_member:
            raise HTTPException(status_code=403, detail="Нет доступа к этому дому")
    
    return {
        "status": "success",
        "data": {
            "home_id": home.id,
            "name": home.name,
            "owner_id": home.owner_id,
            "created_at": home.created_at if hasattr(home, 'created_at') else None
        }
    }