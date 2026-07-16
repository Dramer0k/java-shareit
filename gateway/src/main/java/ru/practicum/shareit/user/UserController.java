package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.OnCreate;

@Controller
@RequestMapping(path = "/users")
@AllArgsConstructor
@Slf4j
@Validated
public class UserController {
    private UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getUser() {
        log.info("GET /users");
        return userClient.getUser();
    }

    @PostMapping
    public ResponseEntity<Object> addUser(@Validated(value = OnCreate.class) @RequestBody UserRequestDto userDto) {
        log.info("POST /users");
        log.info("UserDto: {}", userDto);
        return userClient.addUser(userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        log.info("DELETE /users/{userId}");
        return userClient.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UserRequestDto userDto,
                              @PathVariable Long userId) {
        log.info("PATCH /users/{}", userId);
        return userClient.updateUser(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@PathVariable Long userId) {
        log.info("GET /users/{userId}");
        return userClient.getUserById(userId);
    }
}