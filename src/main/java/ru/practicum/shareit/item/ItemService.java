package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Long userId, Item item);

    Item updateItem(Long userId, Long itemId, Item item);

    Item getItemById(Long itemId);

    List<Item> getItems(Long userId);

    List<Item> getItemsByText(String text);

    List<Item> getAllItems();
}