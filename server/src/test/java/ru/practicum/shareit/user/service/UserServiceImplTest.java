package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyUsedException;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long USER_ID = 1L;
    private static final String EXISTING_EMAIL = "existing@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String UPDATED_NAME = "Updated Name";

    @Test
    void getUsers_returnsAllUsers() {
        List<User> users = List.of(new User(USER_ID, "Name", EXISTING_EMAIL));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getUsers();

        assertEquals(users, result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void addUser_success() {
        User input = new User(null, "Test Name", NEW_EMAIL);
        User saved = new User(USER_ID, "Test Name", NEW_EMAIL);
        when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.addUser(input);

        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_emailAlreadyExists_throwsAlreadyUsedException() {
        User input = new User(null, "Test Name", EXISTING_EMAIL);
        User existing = new User(USER_ID, "Existing Name", EXISTING_EMAIL);
        when(userRepository.findByEmail(EXISTING_EMAIL)).thenReturn(Optional.of(existing));

        AlreadyUsedException thrown = assertThrows(
                AlreadyUsedException.class,
                () -> userService.addUser(input)
        );
        assertTrue(thrown.getMessage().contains("уже существует"));
        verify(userRepository).findByEmail(EXISTING_EMAIL);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_emailIsNull_throwsIncorrectParamsException() {
        User input = new User(null, "Test Name", null);

        IncorrectParamsException thrown = assertThrows(
                IncorrectParamsException.class,
                () -> userService.addUser(input)
        );
        assertTrue(thrown.getMessage().contains("Не указана почта"));
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_emailIsBlank_throwsIncorrectParamsException() {
        User input = new User(null, "Test Name", "   ");

        IncorrectParamsException thrown = assertThrows(
                IncorrectParamsException.class,
                () -> userService.addUser(input)
        );
        assertTrue(thrown.getMessage().contains("Не указана почта"));
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_deletesById() {
        userService.deleteUser(USER_ID);

        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void updateUser_success_updatesNameAndEmail() {
        Long userId = USER_ID;
        User oldUser = new User(userId, "Old Name", "old@example.com");
        User updateDto = new User(null, UPDATED_NAME, NEW_EMAIL);

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updateDto);

        assertNotNull(result);
        assertEquals(UPDATED_NAME, result.getName());
        assertEquals(NEW_EMAIL, result.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(NEW_EMAIL);
        verify(userRepository).save(oldUser);
    }

    @Test
    void updateUser_userNotFound_throwsNotFoundException() {
        Long nonExistingId = 999L;
        User updateDto = new User(null, UPDATED_NAME, NEW_EMAIL);
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(nonExistingId, updateDto)
        );
        assertTrue(thrown.getMessage().contains("не найден"));
        verify(userRepository).findById(nonExistingId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emailAlreadyUsedByOtherUser_throwsAlreadyUsedException() {
        Long userId = USER_ID;
        User oldUser = new User(userId, "Old Name", "old@example.com");
        User otherUser = new User(2L, "Other", EXISTING_EMAIL);
        User updateDto = new User(null, UPDATED_NAME, EXISTING_EMAIL);

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userRepository.findByEmail(EXISTING_EMAIL)).thenReturn(Optional.of(otherUser));

        AlreadyUsedException thrown = assertThrows(
                AlreadyUsedException.class,
                () -> userService.updateUser(userId, updateDto)
        );
        assertTrue(thrown.getMessage().contains("уже существует"));
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(EXISTING_EMAIL);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_updatesOnlyProvidedFields() {
        Long userId = USER_ID;
        User oldUser = new User(userId, "Old Name", "old@example.com");
        User updateDtoWithOnlyName = new User(null, UPDATED_NAME, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updateDtoWithOnlyName);

        assertEquals(UPDATED_NAME, result.getName());
        assertEquals("old@example.com", result.getEmail());

        verify(userRepository).save(oldUser);
    }

    @Test
    void updateUser_emptyNameAndEmail_doesNotChangeFields() {
        Long userId = USER_ID;
        User oldUser = new User(userId, "Old Name", "old@example.com");
        User updateDto = new User(null, "   ", "   "); // пустые/пробельные значения

        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updateDto);

        assertEquals("Old Name", result.getName());
        assertEquals("old@example.com", result.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(oldUser);
    }
}