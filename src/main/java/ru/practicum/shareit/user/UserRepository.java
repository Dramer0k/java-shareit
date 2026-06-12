package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getUsers();

    User addUser(User user);

    void deleteUser(Long userId);

    User updateUser(Long userId, User user);

    Optional<User> getUserById(Long userId);

    User getUserByEmail(String email);
}