from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import text, inspect
from sqlalchemy.orm import Session
from .. import models, config
from ..auth import get_db, get_super_admin
from ..database import Base, engine
import datetime
import os
from typing import Dict, Any

router = APIRouter(prefix="/api/sysadmin", tags=["system-admin"])

# Модель для супер-админа (виртуальная)
SuperAdmin = type('SuperAdmin', (), {
    "username": "admin",
    "role": "super_admin",
    "is_super_admin": True
})

# Список разрешенных таблиц для операций
ALLOWED_TABLES = {
    'users', 'homes', 'devices', 'rooms', 'automations', 
    'home_members', 'sensor_history', 'device_logs'
}

def validate_table_name(table_name: str):
    """Проверка, что таблица разрешена для операций"""
    if table_name not in ALLOWED_TABLES:
        raise HTTPException(
            status_code=400, 
            detail=f"Таблица {table_name} не разрешена для операций. Разрешены: {ALLOWED_TABLES}"
        )

@router.get("/tables")
def get_available_tables(admin: Dict = Depends(get_super_admin)):
    """
    Получение списка доступных таблиц для операций
    """
    return {"available_tables": list(ALLOWED_TABLES)}

@router.get("/tables/{table_name}")
def get_table_data(
    table_name: str,
    skip: int = Query(0, ge=0, description="Смещение"),
    limit: int = Query(100, ge=1, le=1000, description="Лимит"),
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Просмотр данных конкретной таблицы с пагинацией
    """
    validate_table_name(table_name)
    
    try:
        # Получаем общее количество записей
        count_result = db.execute(text(f"SELECT COUNT(*) FROM {table_name}"))
        total_count = count_result.scalar()
        
        # Получаем данные с пагинацией
        result = db.execute(
            text(f"SELECT * FROM {table_name} LIMIT :limit OFFSET :skip"),
            {"limit": limit, "skip": skip}
        )
        
        # Преобразуем в словари
        columns = result.keys()
        rows = [dict(zip(columns, row)) for row in result.fetchall()]
        
        # Преобразуем даты и другие несериализуемые объекты
        for row in rows:
            for key, value in row.items():
                if isinstance(value, (datetime.datetime, datetime.date)):
                    row[key] = value.isoformat()
        
        return {
            "table_name": table_name,
            "total_count": total_count,
            "skip": skip,
            "limit": limit,
            "data": rows
        }
        
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"Ошибка получения данных из таблицы {table_name}: {str(e)}"
        )

@router.get("/tables/{table_name}/structure")
def get_table_structure(
    table_name: str,
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Получение структуры таблицы (колонки, типы, ограничения)
    """
    validate_table_name(table_name)
    
    try:
        inspector = inspect(engine)
        
        # Информация о колонках
        columns = inspector.get_columns(table_name)
        columns_info = []
        
        for col in columns:
            column_info = {
                "name": col["name"],
                "type": str(col["type"]),
                "nullable": col.get("nullable", True),
                "primary_key": col.get("primary_key", False),
                "default": str(col.get("default", ""))
            }
            columns_info.append(column_info)
        
        # Информация о первичных ключах
        pk_info = inspector.get_pk_constraint(table_name)
        
        # Информация о внешних ключах
        fk_info = inspector.get_foreign_keys(table_name)
        
        # Информация об индексах
        indexes_info = inspector.get_indexes(table_name)
        
        return {
            "table_name": table_name,
            "columns": columns_info,
            "primary_key": pk_info,
            "foreign_keys": fk_info,
            "indexes": indexes_info
        }
        
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"Ошибка получения структуры таблицы {table_name}: {str(e)}"
        )

@router.delete("/tables/{table_name}/rows/{row_id}")
def delete_table_row(
    table_name: str,
    row_id: int,
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Удаление конкретной строки из таблицы по ID
    """
    validate_table_name(table_name)
    
    try:
        # Сначала проверяем существование записи
        check_result = db.execute(
            text(f"SELECT COUNT(*) FROM {table_name} WHERE id = :id"),
            {"id": row_id}
        )
        
        if check_result.scalar() == 0:
            raise HTTPException(
                status_code=404, 
                detail=f"Запись с ID {row_id} не найдена в таблице {table_name}"
            )
        
        # Получаем данные записи перед удалением (для логгирования)
        record_result = db.execute(
            text(f"SELECT * FROM {table_name} WHERE id = :id"),
            {"id": row_id}
        )
        record_data = dict(zip(record_result.keys(), record_result.fetchone()))
        
        # Выполняем удаление
        delete_result = db.execute(
            text(f"DELETE FROM {table_name} WHERE id = :id"),
            {"id": row_id}
        )
        
        db.commit()
        
        return {
            "status": "success",
            "message": f"Запись с ID {row_id} удалена из таблицы {table_name}",
            "deleted_record": record_data,
            "affected_rows": delete_result.rowcount
        }
        
    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=500, 
            detail=f"Ошибка удаления записи из таблицы {table_name}: {str(e)}"
        )

@router.delete("/tables/{table_name}/clear")
def clear_table(
    table_name: str,
    confirmation: str = Query(..., description="Подтверждение: введите 'DELETE_ALL'"),
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Полная очистка таблицы (ОПАСНАЯ ОПЕРАЦИЯ)
    """
    validate_table_name(table_name)
    
    if confirmation != "DELETE_ALL":
        raise HTTPException(
            status_code=400,
            detail="Требуется подтверждение: введите 'DELETE_ALL' в параметре confirmation"
        )
    
    try:
        # Получаем количество записей перед очисткой
        count_result = db.execute(text(f"SELECT COUNT(*) FROM {table_name}"))
        records_count = count_result.scalar()
        
        # Выполняем очистку
        delete_result = db.execute(text(f"DELETE FROM {table_name}"))
        db.commit()
        
        return {
            "status": "success",
            "message": f"Таблица {table_name} полностью очищена",
            "deleted_records": records_count,
            "affected_rows": delete_result.rowcount
        }
        
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=500, 
            detail=f"Ошибка очистки таблицы {table_name}: {str(e)}"
        )

@router.post("/tables/{table_name}/query")
def execute_custom_query(
    table_name: str,
    query: str,
    params: Dict[str, Any] = None,
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Выполнение кастомного SQL запроса для конкретной таблицы
    ВНИМАНИЕ: Только SELECT запросы!
    """
    validate_table_name(table_name)
    
    # Безопасность: проверяем, что это SELECT запрос
    query_upper = query.strip().upper()
    if not query_upper.startswith('SELECT'):
        raise HTTPException(
            status_code=400,
            detail="Разрешены только SELECT запросы"
        )
    
    try:
        # Выполняем запрос
        result = db.execute(
            text(query),
            params or {}
        )
        
        # Преобразуем результат
        columns = result.keys()
        rows = [dict(zip(columns, row)) for row in result.fetchall()]
        
        # Преобразуем даты
        for row in rows:
            for key, value in row.items():
                if isinstance(value, (datetime.datetime, datetime.date)):
                    row[key] = value.isoformat()
        
        return {
            "table_name": table_name,
            "query": query,
            "params": params,
            "result_count": len(rows),
            "data": rows
        }
        
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"Ошибка выполнения запроса: {str(e)}"
        )

@router.get("/tables/{table_name}/search")
def search_in_table(
    table_name: str,
    column: str,
    value: str,
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Поиск в конкретной таблице по значению в колонке
    """
    validate_table_name(table_name)
    
    try:
        # Проверяем существование колонки
        inspector = inspect(engine)
        columns = [col['name'] for col in inspector.get_columns(table_name)]
        
        if column not in columns:
            raise HTTPException(
                status_code=400,
                detail=f"Колонка '{column}' не найдена в таблице {table_name}. Доступные колонки: {columns}"
            )
        
        # Выполняем поиск
        result = db.execute(
            text(f"SELECT * FROM {table_name} WHERE {column} LIKE :value"),
            {"value": f"%{value}%"}
        )
        
        # Преобразуем результат
        result_columns = result.keys()
        rows = [dict(zip(result_columns, row)) for row in result.fetchall()]
        
        # Преобразуем даты
        for row in rows:
            for key, value in row.items():
                if isinstance(value, (datetime.datetime, datetime.date)):
                    row[key] = value.isoformat()
        
        return {
            "table_name": table_name,
            "search_column": column,
            "search_value": value,
            "result_count": len(rows),
            "data": rows
        }
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500, 
            detail=f"Ошибка поиска в таблице {table_name}: {str(e)}"
        )

# Остальные существующие эндпоинты остаются без изменений...

@router.get("/database/tables")
def get_database_tables(
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Получение информации о всех таблицах в БД
    Только для супер-администратора
    """
    from sqlalchemy import inspect
    
    inspector = inspect(engine)
    tables = inspector.get_table_names()
    
    table_info = []
    for table in tables:
        columns = inspector.get_columns(table)
        # Безопасный подсчет строк
        try:
            row_count = db.execute(text(f"SELECT COUNT(*) FROM {table}")).scalar()
        except:
            row_count = 0
            
        table_info.append({
            "table_name": table,
            "columns": [
                {
                    "name": col["name"], 
                    "type": str(col["type"]),
                    "nullable": col.get("nullable", True)
                } for col in columns
            ],
            "row_count": row_count
        })
    
    return {"tables": table_info}

@router.get("/database/stats")
def get_database_stats(
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Получение статистики базы данных
    Только для супер-администратора
    """
    stats = {}
    
    # Статистика по таблицам
    tables = ["users", "homes", "devices", "rooms", "automations", "home_members"]
    
    for table in tables:
        try:
            count = db.execute(text(f"SELECT COUNT(*) FROM {table}")).scalar()
            stats[table] = count
        except:
            stats[table] = 0
    
    # Размер базы данных (для SQLite)
    if config.DATABASE_URL.startswith("sqlite"):
        try:
            db_file = config.DATABASE_URL.replace("sqlite:///", "")
            size = os.path.getsize(db_file) if os.path.exists(db_file) else 0
            stats["database_size_bytes"] = size
            stats["database_size_mb"] = round(size / (1024 * 1024), 2)
        except:
            stats["database_size"] = "unknown"
    
    return stats

@router.post("/database/backup")
def create_database_backup(
    admin: Dict = Depends(get_super_admin)
):
    """
    Создание резервной копии базы данных
    Только для супер-администратора
    """
    try:
        timestamp = datetime.datetime.utcnow().strftime('%Y%m%d_%H%M%S')
        backup_file = f"backup_{timestamp}.db"
        
        # Для SQLite просто копируем файл
        if config.DATABASE_URL.startswith("sqlite"):
            import shutil
            db_file = config.DATABASE_URL.replace("sqlite:///", "")
            
            if os.path.exists(db_file):
                shutil.copy2(db_file, backup_file)
                
                return {
                    "status": "success",
                    "message": "Резервная копия создана",
                    "backup_file": backup_file,
                    "size_bytes": os.path.getsize(backup_file)
                }
            else:
                raise HTTPException(status_code=404, detail="Файл базы данных не найден")
        else:
            return {
                "status": "error", 
                "message": "Резервное копирование поддерживается только для SQLite"
            }
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка создания бэкапа: {str(e)}")
    
@router.post("/database/cleanup")
def cleanup_database(
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    Очистка устаревших данных (например, старых логов)
    Только для супер-администратора
    """
    try:
        # Пример: удаление записей старше 30 дней из истории сенсоров
        from datetime import datetime, timedelta
        cutoff_date = datetime.utcnow() - timedelta(days=30)
        
        # Если есть таблица sensor_history
        try:
            deleted = db.execute(
                text("DELETE FROM sensor_history WHERE created_at < :cutoff"),
                {"cutoff": cutoff_date}
            ).rowcount
        except:
            deleted = 0
        
        db.commit()
        
        return {
            "status": "success",
            "message": f"Удалено {deleted} устаревших записей",
            "cutoff_date": cutoff_date.isoformat()
        }
        
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Ошибка очистки: {str(e)}")

@router.get("/system/info")
def get_system_info(
    admin: Dict = Depends(get_super_admin)
):
    """
    Получение информации о системе
    Только для супер-администратора
    """
    import platform
    import psutil
    
    try:
        # Информация о системе
        system_info = {
            "platform": platform.platform(),
            "python_version": platform.python_version(),
            "hostname": platform.node(),
            "processor": platform.processor(),
            "system_uptime": psutil.boot_time(),
        }
        
        # Использование памяти
        memory = psutil.virtual_memory()
        system_info["memory"] = {
            "total_gb": round(memory.total / (1024**3), 2),
            "available_gb": round(memory.available / (1024**3), 2),
            "used_percent": memory.percent
        }
        
        # Дисковое пространство
        disk = psutil.disk_usage('/')
        system_info["disk"] = {
            "total_gb": round(disk.total / (1024**3), 2),
            "free_gb": round(disk.free / (1024**3), 2),
            "used_percent": disk.percent
        }
        
        return system_info
        
    except Exception as e:
        return {
            "status": "error",
            "message": f"Не удалось получить информацию о системе: {str(e)}"
        }

@router.delete("/database/reset")
def reset_database(
    confirmation: str,
    admin: Dict = Depends(get_super_admin),
    db: Session = Depends(get_db)
):
    """
    ПОЛНЫЙ СБРОС БАЗЫ ДАННЫХ (ОПАСНО!)
    Требуется подтверждение строкой "RESET_ALL_DATA"
    """
    if confirmation != "RESET_ALL_DATA":
        raise HTTPException(
            status_code=400,
            detail="Требуется подтверждение с текстом 'RESET_ALL_DATA'"
        )
    
    try:
        # Создаем резервную копию перед сбросом
        timestamp = datetime.datetime.utcnow().strftime('%Y%m%d_%H%M%S')
        backup_file = f"pre_reset_backup_{timestamp}.db"
        
        if config.DATABASE_URL.startswith("sqlite"):
            import shutil
            db_file = config.DATABASE_URL.replace("sqlite:///", "")
            if os.path.exists(db_file):
                shutil.copy2(db_file, backup_file)
        
        # Удаляем все таблицы
        Base.metadata.drop_all(bind=engine)
        
        # Создаем заново
        Base.metadata.create_all(bind=engine)
        
        return {
            "status": "success",
            "message": "База данных полностью сброшена и пересоздана",
            "backup_created": backup_file if os.path.exists(backup_file) else False
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка сброса БД: {str(e)}")

@router.get("/system/logs")
def get_system_logs(
    lines: int = 100,
    admin: models.User = Depends(get_super_admin)
):
    """
    Просмотр системных логов
    Только для супер-администратора
    """
    try:
        # Чтение логов приложения
        log_files = ["app.log", "mqtt.log", "automation.log"]
        logs = {}
        
        for log_file in log_files:
            if os.path.exists(log_file):
                with open(log_file, "r") as f:
                    logs[log_file] = f.readlines()[-lines:]
            else:
                logs[log_file] = [f"Файл {log_file} не найден"]
                
        return logs
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка чтения логов: {e}")