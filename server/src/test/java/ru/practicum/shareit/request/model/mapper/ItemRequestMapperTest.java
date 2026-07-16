package ru.practicum.shareit.request.model.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.dto.ItemInRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestMapperTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void toItemRequest() {
        String description = "Хочу взять дрель";
        User user = new User(1L, "Alex", "alex@example.com");

        ItemRequestDto dto = new ItemRequestDto(description);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);

        assertThat(request.getId()).isNull();
        assertThat(request.getDescription()).isEqualTo(description);
        assertThat(request.getRequestor()).isEqualTo(user);
        assertThat(request.getCreated()).isNotNull();
        assertThat(request.getCreated())
                .isBetween(LocalDateTime.now().minusSeconds(5), LocalDateTime.now());
    }

    @Test
    void toResponse() {
        Long id = 100L;
        String description = "Запрос на дрель";
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 12, 0);
        User requestor = new User(5L, "Maria", "maria@example.com");

        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);

        ItemRequestResponse response = ItemRequestMapper.toResponse(request);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getDescription()).isEqualTo(description);
        assertThat(response.getRequestor()).isEqualTo(requestor);
        assertThat(response.getCreated()).isEqualTo(created);
    }

    @Test
    void toItemInRequest() {
        Long itemId = 7L;
        String itemName = "Дрель аккумуляторная";
        Long ownerId = 3L;

        User owner = new User(ownerId, "Owner", "owner@example.com");
        Item item = new Item();
        item.setId(itemId);
        item.setName(itemName);
        item.setOwner(owner);

        ItemInRequest result = ItemRequestMapper.toItemInRequest(item);

        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getName()).isEqualTo(itemName);
        assertThat(result.getOwner()).isEqualTo(ownerId);
    }

    @Test
    void toRequestWithAnswer_noItems() {
        Long requestId = 200L;
        String description = "Пустой запрос";
        LocalDateTime created = LocalDateTime.of(2024, 2, 1, 10, 30);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription(description);
        request.setCreated(created);

        when(itemRepository.findAllByRequestId(requestId)).thenReturn(List.of());

        ItemRequestWithAnswer result = ItemRequestMapper.toRequestWithAnswer(request, itemRepository);

        assertThat(result.getId()).isEqualTo(requestId);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCreated()).isEqualTo(created);
        assertThat(result.getItems()).isEmpty();

        verify(itemRepository).findAllByRequestId(requestId);
    }

    @Test
    void toRequestWithAnswer_withItems() {
        Long requestId = 201L;
        String description = "Запрос с предметами";
        LocalDateTime created = LocalDateTime.of(2024, 3, 1, 9, 15);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription(description);
        request.setCreated(created);

        Long item1Id = 11L;
        Long item2Id = 12L;
        User owner1 = new User(4L, "Owner1", "o1@example.com");
        User owner2 = new User(5L, "Owner2", "o2@example.com");

        Item item1 = new Item();
        item1.setId(item1Id);
        item1.setName("Дрель");
        item1.setOwner(owner1);

        Item item2 = new Item();
        item2.setId(item2Id);
        item2.setName("Шуруповёрт");
        item2.setOwner(owner2);

        List<Item> items = List.of(item1, item2);
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(items);

        ItemRequestWithAnswer result = ItemRequestMapper.toRequestWithAnswer(request, itemRepository);

        assertThat(result.getId()).isEqualTo(requestId);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getCreated()).isEqualTo(created);
        assertThat(result.getItems()).hasSize(2);

        ItemInRequest firstItem = result.getItems().getFirst();
        assertThat(firstItem.getId()).isEqualTo(item1Id);
        assertThat(firstItem.getName()).isEqualTo("Дрель");
        assertThat(firstItem.getOwner()).isEqualTo(owner1.getId());

        ItemInRequest secondItem = result.getItems().get(1);
        assertThat(secondItem.getId()).isEqualTo(item2Id);
        assertThat(secondItem.getName()).isEqualTo("Шуруповёрт");
        assertThat(secondItem.getOwner()).isEqualTo(owner2.getId());

        verify(itemRepository).findAllByRequestId(requestId);
    }
}