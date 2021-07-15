package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message> {
    List<Message> findMessagesByRoom(Long id);
}