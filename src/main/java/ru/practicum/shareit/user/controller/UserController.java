package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping
    public List<User> getUser() {
        return userService.getUsers();
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        User user = userService.addUser(UserMapper.toUser(userDto));
        return UserMapper.toDto(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@Valid @RequestBody UserDto userDto,
                              @PathVariable Long userId) {
        User user = userService.updateUser(userId, UserMapper.toUser(userDto));

        return UserMapper.toDto(user);
    }

    @GetMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);

        return UserMapper.toDto(user);
    }
}