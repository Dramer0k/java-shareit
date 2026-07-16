package ru.practicum.shareit.user.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class IntegrationUserServiceTest {
    private final EntityManager em;
    private final UserService service;

    private User user;

    @BeforeEach
    public void beforeEach() {
        user = UserMapper.toUser(generateUserDto());
        service.addUser(user);
    }

    @Test
    public void saveUserTest() {
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User userQuery = query.setParameter("email", user.getEmail()).getSingleResult();

        assertNotNull(user.getId());
        assertEquals(userQuery.getName(), user.getName());
        assertEquals(userQuery.getEmail(), user.getEmail());
    }

    @Test
    public void getUserById() {
        User result = service.getUserById(user.getId());

        assertNotNull(result.getId());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());
    }

    private UserDto generateUserDto() {
        UserDto dto = new UserDto();
        dto.setEmail("user@email.com");
        dto.setName("user");
        return dto;
    }

}