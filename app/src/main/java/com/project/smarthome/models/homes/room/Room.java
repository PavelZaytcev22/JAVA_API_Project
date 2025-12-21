package com.project.smarthome.models.homes.room;

public class Room {

    private final int id;
    private final String name;
    private final int homeId;

    public Room(int id, String name, int homeId) {
        this.id = id;
        this.name = name;
        this.homeId = homeId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHomeId() {
        return homeId;
    }

    public static Room fromResponse(RoomResponse response) {
        return new Room(
                response.getId(),
                response.getName(),
                response.getHomeId()
        );
    }
}
