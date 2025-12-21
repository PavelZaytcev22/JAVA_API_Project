package com.project.smarthome.models.homes.room;

import java.util.List;

public interface RoomRepository {

    /* ===== Загрузка комнат дома ===== */
    interface LoadRoomsCallback {
        void onSuccess(List<Room> rooms);
        void onError(Throwable throwable);
    }

    /* ===== Создание комнаты ===== */
    interface CreateRoomCallback {
        void onSuccess(Room room);
        void onError(Throwable throwable);
    }

    /**
     * Загрузить список комнат для конкретного дома
     *
     * @param homeId   идентификатор дома
     * @param callback результат операции
     */
    void loadRooms(int homeId, LoadRoomsCallback callback);

    /**
     * Создать новую комнату в доме
     *
     * @param homeId   идентификатор дома
     * @param name     название комнаты
     * @param callback результат операции
     */
    void createRoom(int homeId, String name, CreateRoomCallback callback);
}
