package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyUsedException;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.getUsers();
    }

    @Override
    public User addUser(User user) {
        log.info("Создаем пользователя...");
        log.info("Входные данные: {}", user);
        validationUsedEmail(user);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IncorrectParamsException("Не указана почта пользователя!");
        }
        return userRepository.addUser(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаляем пользователя с id {}", userId);
        userRepository.deleteUser(userId);
    }

    @Override
    public User updateUser(Long userId, User user) {
        if (userRepository.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id" + userId + " не найден!");
        }

        log.info("Обновляем данные пользователя {}", userId);
        log.info("Необходимо внести изменения: {}", user);

        validationUsedEmail(user);

        return userRepository.updateUser(userId, user);
    }

    public void validationUsedEmail(User user) {
        log.info("Проверка наличия пользователя с Email: {}...", user.getEmail());
        if (userRepository.getUserByEmail(user.getEmail()) != null) {
            throw new AlreadyUsedException("Пользователь с таким имейлом уже существует!");
        }
    }

    public User getUserById(Long userId) {
        return userRepository.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
    }
}