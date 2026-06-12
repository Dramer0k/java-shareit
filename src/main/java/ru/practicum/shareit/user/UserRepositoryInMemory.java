package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepositoryInMemory implements UserRepository {
    Map<Long, User> users = new HashMap<>();
    Long index = 1L;

    @Override
    public List<User> getUsers() {
        return users.values().stream()
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    @Override
    public User addUser(User user) {
        user.setId(index++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User oldUser = users.get(userId);
        log.info("Старые данные пользователя: {}", oldUser);
        if (user.getName() == null) {
            user.setName(oldUser.getName());
        }

        if (user.getEmail() == null) {
            user.setEmail(oldUser.getEmail());
        }

        user.setId(userId);
        users.put(userId, user);
        return user;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User getUserByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}