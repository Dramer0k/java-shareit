package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyUsedException;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User addUser(User user) {
        log.info("Создаем пользователя...");
        log.info("Входные данные: {}", user);

        checkEmailUsage(user);
        checkAvailabilityEmail(user);

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаляем пользователя с id {}", userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User oldUser = getUserById(userId);

        log.info("Обновляем данные пользователя {}", userId);
        log.info("Необходимо внести изменения: {}", user);

        checkEmailUsage(user);

        updateUser(oldUser, user);

        return userRepository.save(oldUser);
    }

    public User getUserById(Long userId) {
        return  userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
    }

    private void checkEmailUsage(User user) {
        log.info("Проверка наличия пользователя с Email: {}...", user.getEmail());
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new AlreadyUsedException("Пользователь с таким имейлом уже существует!");
        }
    }

    private void checkAvailabilityEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IncorrectParamsException("Не указана почта пользователя!");
        }
    }

    public void updateUser(User user1, User user2) {
        if (user2.getName() != null && !user2.getName().isBlank()) {
            user1.setName(user2.getName());
        }

        if ((user2.getEmail() != null && !user2.getEmail().isBlank())) {
            user1.setEmail(user2.getEmail());
        }
    }
}