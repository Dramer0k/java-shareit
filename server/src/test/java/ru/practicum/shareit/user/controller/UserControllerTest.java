package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    private UserDto userDto;
    private List<UserDto> users;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "John", "john.doe@mail.com");

        users = List.of(
                new UserDto(1L, "John", "john.doe@mail.com"),
                new UserDto(2L, "Jane", "jane.doe@mail.com")
        );
    }

    @Test
    void getUsers_success() throws Exception {
        when(userService.getUsers())
                .thenReturn(users.stream().map(UserMapper::toUser).toList());

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[0].email", is("john.doe@mail.com")))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(userService).getUsers();
    }

    @Test
    void addUser_success() throws Exception {
        UserDto created = new UserDto(null, "Alice", "alice@example.com");
        User createdUser = UserMapper.toUser(created);
        createdUser.setId(3L);
        created = UserMapper.toDto(createdUser);

        when(userService.addUser(any()))
                .thenReturn(createdUser);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(created))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));

        verify(userService).addUser(any());
    }

    @Test
    void updateUser_success() throws Exception {
        UserDto updated = new UserDto(1L, "John Updated", "john.updated@mail.com");
        User updatedUser = UserMapper.toUser(updated);

        when(userService.updateUser(eq(1L), any()))
                .thenReturn(updatedUser);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@mail.com")));

        verify(userService).updateUser(eq(1L), any());
    }

    @Test
    void getUserById_success() throws Exception {
        User user = UserMapper.toUser(userDto);
        when(userService.getUserById(eq(1L))).thenReturn(user);

        mvc.perform(get("/users/1").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john.doe@mail.com")));

        verify(userService).getUserById(eq(1L));
    }

    @Test
    void deleteUser_success() throws Exception {
        mvc.perform(delete("/users/1"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).deleteUser(eq(1L));
    }

    @Test
    void addUser_validationError_nameEmpty() throws Exception {
        UserDto invalid = new UserDto(null, "", "no-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(any());
    }

    @Test
    void addUser_validationError_emailInvalid() throws Exception {
        UserDto invalid = new UserDto(null, "Bob", "not-an-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userService.getUserById(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/users/999").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;

        verify(userService).getUserById(eq(999L));
    }

    @Test
    void getUsers_emptyList() throws Exception {
        when(userService.getUsers()).thenReturn(List.of());

        mvc.perform(get("/users").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService).getUsers();
    }
}