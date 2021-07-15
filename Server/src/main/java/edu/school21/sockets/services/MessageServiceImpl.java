package edu.school21.sockets.services;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.Room;
import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.MessageRepository;
import edu.school21.sockets.repositories.RoomsRepository;
import edu.school21.sockets.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class MessageServiceImpl implements MessageService {
    private final UsersRepository usersRepository;
    private final MessageRepository messageRepository;
    private final RoomsRepository roomsRepository;

    public MessageServiceImpl(UsersRepository usersRepository, MessageRepository messageRepository, RoomsRepository roomsRepository) {
        this.usersRepository = usersRepository;
        this.messageRepository = messageRepository;
        this.roomsRepository = roomsRepository;
    }

    @Override
    public void saveMessage(String msg, String username, String roomName) {
        User user = usersRepository.findByUsername(username).orElse(null);
        Room room = roomsRepository.findByName(roomName).orElse(null);
        Message message = new Message(null, user, room, msg, new Timestamp(System.currentTimeMillis()));
        messageRepository.save(message);
    }
}