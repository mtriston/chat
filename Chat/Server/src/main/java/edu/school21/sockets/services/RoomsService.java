package edu.school21.sockets.services;

import edu.school21.sockets.models.Room;

import java.util.List;
import java.util.Optional;

public interface RoomsService {
    void saveRoom(String name, String owner);
    Optional<Room> getRoomByName(String name);
    List<Room> getAllRooms();
}
