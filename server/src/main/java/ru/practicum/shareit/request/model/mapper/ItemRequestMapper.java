package ru.practicum.shareit.request.model.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.dto.ItemInRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User user) {
        ItemRequest request = new ItemRequest();
        request.setDescription(itemRequestDto.getDescription());
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestResponse toResponse(ItemRequest itemRequest) {
        return new ItemRequestResponse(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequestWithAnswer toRequestWithAnswer(ItemRequest itemRequest, List<ItemInRequest> itemList) {
        ItemRequestWithAnswer request = new ItemRequestWithAnswer();
        request.setId(itemRequest.getId());
        request.setDescription(itemRequest.getDescription());
        request.setCreated(itemRequest.getCreated());
        request.setItems(itemList);

        return request;
    }

    public static ItemInRequest toItemInRequest(Item item) {
        ItemInRequest result = new ItemInRequest();
        result.setId(item.getId());
        result.setName(item.getName());
        result.setOwner(item.getOwner().getId());

        return result;
    }
}