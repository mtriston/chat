package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.Room;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomsRepositoryImpl implements RoomsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final UsersRepository usersRepository;

    final String findByIdQuery = "SELECT * FROM chat.rooms WHERE id = ?";
    final String findAllQuery = "SELECT * FROM chat.rooms";
    final String updateQuery = "UPDATE chat.rooms SET name = ?, owner = ? WHERE id = ?";
    final String saveQuery = "INSERT INTO chat.rooms(name, owner) VALUES (?, ?)";
    final String deleteQuery = "DELETE FROM chat.messages WHERE id = ?";
    final String findByNameQuery = "SELECT * FROM chat.rooms WHERE name = ?";

    public RoomsRepositoryImpl(DataSource dataSource, UsersRepository usersRepository) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.usersRepository = usersRepository;
    }

    private Message mapRowToMessage(ResultSet resultSet, int rowNum) throws SQLException {

        return new Message(
                resultSet.getLong("id"),
                usersRepository.findById(resultSet.getLong("sender")),
                null,
                resultSet.getString("text"),
                resultSet.getTimestamp("time")
        );
    }

    private Room mapRowToRoom(ResultSet resultSet, int rowNum) throws SQLException {
        Room room = new Room(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                usersRepository.findById(resultSet.getLong("owner")),
                jdbcTemplate.query("SELECT * FROM chat.messages WHERE room = " + resultSet.getLong("id"), this::mapRowToMessage)
        );
        room.getMessages().forEach(e -> e.setRoom(room));
        return room;
    }

    @Override
    public Room findById(Long id) {
        return jdbcTemplate.queryForObject(findByIdQuery, this::mapRowToRoom, id);
    }

    @Override
    public List<Room> findAll() {
        return jdbcTemplate.query(findAllQuery, this::mapRowToRoom);
    }

    @Override
    public void save(Room entity) {
        jdbcTemplate.update(saveQuery, entity.getName(), entity.getOwner().getId());
    }

    @Override
    public void update(Room entity) {
        jdbcTemplate.update(updateQuery, entity.getName(), entity.getOwner().getId(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(deleteQuery, id);
    }

    @Override
    public Optional<Room> findByName(String name) {
        Optional<Room> room = Optional.empty();
        try {
            room = Optional.ofNullable(jdbcTemplate.queryForObject(findByNameQuery, this::mapRowToRoom, name));
        } catch (EmptyResultDataAccessException ignored) {
        }
        return room;
    }
}
