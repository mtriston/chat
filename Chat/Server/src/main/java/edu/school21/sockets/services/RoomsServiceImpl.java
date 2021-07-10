package edu.school21.sockets.services;

import edu.school21.sockets.models.Room;
import edu.school21.sockets.repositories.RoomsRepository;
import edu.school21.sockets.repositories.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomsServiceImpl implements RoomsService {
    private final UsersRepository usersRepository;
    private final RoomsRepository roomsRepository;

    public RoomsServiceImpl(UsersRepository usersRepository, RoomsRepository roomsRepository) {
        this.usersRepository = usersRepository;
        this.roomsRepository = roomsRepository;
    }

    @Override
    public void saveRoom(String name, String owner) {
        Room room = new Room(null, name, usersRepository.findByUsername(owner).orElse(null), null);
        roomsRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomByName(String name) {
        return roomsRepository.findByName(name);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomsRepository.findAll();
    }


}
