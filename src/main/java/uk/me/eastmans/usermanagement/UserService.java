package uk.me.eastmans.usermanagement;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.me.eastmans.security.User;
import uk.me.eastmans.security.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    UserService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteUser(User user) {
        userRepository.delete(user);
        userRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<User> list(Pageable pageable) {
        return userRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void saveOrCreate(User user) {
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public Optional<User> getUser(long id) { return userRepository.findById(String.valueOf(id));}
}
