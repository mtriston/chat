package edu.school21.sockets.services;

import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersServiceImpl implements UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder encoder;

    public UsersServiceImpl(UsersRepository usersRepository, PasswordEncoder encoder) {
        this.usersRepository = usersRepository;
        this.encoder = encoder;
    }

    @Override
    public boolean signUp(String username, String password) {
        Optional<User> user = usersRepository.findByUsername(username);
        if (user.isPresent())
            return false;
        usersRepository.save(new User(null, username, encoder.encode(password)));
        return true;
    }

    @Override
    public boolean signIn(String username, String password) {
        Optional<User> user = usersRepository.findByUsername(username);
        return user.isPresent() && encoder.matches(password, user.get().getPassword());
    }
}
