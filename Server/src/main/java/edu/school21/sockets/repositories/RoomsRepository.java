package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Room;

import java.util.Optional;

public interface RoomsRepository extends CrudRepository<Room> {
    Optional<Room> findByName(String name);
}
