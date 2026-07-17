package ru.practicum.shareit.item.model.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toDto_success() {
        Long itemId = 100L;
        String name = "Laptop";
        String description = "Good laptop for work";
        boolean available = true;
        Long ownerId = 200L;
        Long requestId = 300L;

        User owner = new User(ownerId, "Owner", "owner@example.com");
        Item item = new Item();
        item.setId(itemId);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(new ItemRequest(requestId, description, owner, LocalDateTime.now()));

        ItemDto dto = ItemMapper.toDto(item);

        assertThat(dto.getId()).isEqualTo(itemId);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getAvailable()).isEqualTo(available);
        assertThat(dto.getOwner()).isEqualTo(ownerId);
        assertThat(dto.getRequestId()).isEqualTo(requestId);
    }

    @Test
    void toDto_withNullFields() {
        Item item = new Item();
        item.setId(999L);

        ItemDto dto = ItemMapper.toDto(item);

        assertThat(dto.getId()).isEqualTo(999L);
        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getOwner()).isNull();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void toItem_success() {
        Long itemId = 101L;
        String name = "Headphones";
        String description = "Noise-cancelling headphones";
        boolean available = false;
        Long ownerId = 201L;
        Long requestId = 401L;

        ItemDto dto = new ItemDto();
        dto.setId(itemId);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setOwner(ownerId);
        dto.setRequestId(requestId);

        Item item = ItemMapper.toItem(dto);

        assertThat(item.getId()).isEqualTo(itemId);
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getDescription()).isEqualTo(description);
        assertThat(item.getAvailable()).isEqualTo(available);
    }

    @Test
    void toItem_withNullOwnerId_andNullRequestId() {
        Long itemId = 102L;
        String name = "Keyboard";
        ItemDto dto = new ItemDto();
        dto.setId(itemId);
        dto.setName(name);
        dto.setOwner(null);
        dto.setRequestId(null);

        Item item = ItemMapper.toItem(dto);

        assertThat(item.getId()).isEqualTo(itemId);
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getOwner()).isNull();
        assertThat(item.getRequest()).isNull();
    }

    @Test
    void toDto_andBack_roundTrip() {
        Long itemId = 500L;
        String name = "Mouse";
        String desc = "Wireless mouse";
        boolean available = true;
        Long ownerId = 600L;
        Long reqId = 700L;

        User owner = new User(ownerId, "O", "o@example.com");
        ItemRequest req = new ItemRequest(reqId, desc, owner, LocalDateTime.now());

        Item original = new Item();
        original.setId(itemId);
        original.setName(name);
        original.setDescription(desc);
        original.setAvailable(available);
        original.setOwner(owner);
        original.setRequest(req);

        ItemDto dto = ItemMapper.toDto(original);
        Item mappedBack = ItemMapper.toItem(dto);

        assertThat(mappedBack.getId()).isEqualTo(original.getId());
        assertThat(mappedBack.getName()).isEqualTo(original.getName());
        assertThat(mappedBack.getDescription()).isEqualTo(original.getDescription());
        assertThat(mappedBack.getAvailable()).isEqualTo(original.getAvailable());
    }
}