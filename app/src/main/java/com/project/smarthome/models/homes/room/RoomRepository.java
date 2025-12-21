package com.project.smarthome.models.homes.room;

import java.util.List;

public interface RoomRepository {

    interface LoadRoomsCallback {
        void onSuccess(List<Room> rooms);
        void onError(Throwable throwable);
    }

    interface CreateRoomCallback {
        void onSuccess(Room room);
        void onError(Throwable throwable);
    }

    void loadRooms(LoadRoomsCallback callback);

    void createRoom(String name, CreateRoomCallback callback);
}
