package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.dto.ResponseWithComment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Long userId, Item item);

    Item updateItem(Long userId, Long itemId, Item item);

    ResponseWithComment getItemById(Long itemId);

    List<ResponseWithBookingData> getItems(Long userId);

    List<Item> searchItems(String text);

    List<Item> getAllItems();

    CommentResponse setComment(Long userId, Long itemId, CommentRequest comment);
}