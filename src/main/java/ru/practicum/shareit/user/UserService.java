package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    List<User> getUsers();

    User addUser(User user);

    void deleteUser(Long userId);

    User updateUser(Long userId, User user);

    User getUserById(Long userId);
}