package edu.school21.sockets.repositories;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import edu.school21.sockets.models.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UsersRepositoryImpl implements UsersRepository {

    private final JdbcTemplate jdbcTemplate;

    final String findByIdQuery = "SELECT * FROM chat.users WHERE id = ?";
    final String findAllQuery = "SELECT * FROM chat.users";
    final String updateQuery = "UPDATE chat.users SET username = ?, password = ? WHERE id = ?";
    final String saveQuery = "INSERT INTO chat.users(username, password) VALUES (?, ?)";
    final String deleteQuery = "DELETE FROM chat.users WHERE id = ?";
    final String findByUsernameQuery = "SELECT * FROM chat.users WHERE username = ?";

    public UsersRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("password")
        );
    }

    @Override
    public User findById(Long id) {
        return jdbcTemplate.queryForObject(findByIdQuery, this::mapRowToUser, id);
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(findAllQuery, this::mapRowToUser);
    }

    @Override
    public void save(User entity) {
        jdbcTemplate.update(saveQuery, entity.getUsername(), entity.getPassword());
    }

    @Override
    public void update(User entity) {
        jdbcTemplate.update(updateQuery, entity.getUsername(), entity.getPassword(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(deleteQuery, id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Optional<User> user = Optional.empty();
        try {
            user = Optional.ofNullable(jdbcTemplate.queryForObject(findByUsernameQuery, this::mapRowToUser, username));
        } catch (EmptyResultDataAccessException ignored) {
        }
        return user;
    }
}
