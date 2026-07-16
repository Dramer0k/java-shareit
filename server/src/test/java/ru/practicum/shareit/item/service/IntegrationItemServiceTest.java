package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ResponseWithComment;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IntegrationItemServiceTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    private ItemDto itemDto;
    private Item item;

    @BeforeEach
    public void beforeEach() {
        UserDto userDto = generateUserDto("user", "user@email.com");
        itemDto = generateItemDto();
        User user = userService.addUser(UserMapper.toUser(userDto));
        item = itemService.addItem(user.getId(), ItemMapper.toItem(itemDto));
    }

    @Test
    public void addUserTest() {
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item item = query.setParameter("name", itemDto.getName()).getSingleResult();

        assertNotNull(item.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), (itemDto.getDescription()));
    }

    @Test
    public void findItemByIdTest() {
        ResponseWithComment searchedItemDto = itemService.getItemById(item.getId());

        assertNotNull(searchedItemDto);
        assertEquals(item.getName(), searchedItemDto.getName());
        assertEquals(item.getDescription(), searchedItemDto.getDescription());
        assertEquals(item.getAvailable(), searchedItemDto.getAvailable());
    }

    private ItemDto generateItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("itemName");
        itemDto.setDescription("description");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private UserDto generateUserDto(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}