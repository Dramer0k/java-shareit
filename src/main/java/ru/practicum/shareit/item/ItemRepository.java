package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateItem(Long itemId, Item item);

    Optional<Item> getItemById(Long itemId);

    List<Item> getItems(Long userID);

    List<Item> getItemsByText(String text);

    List<Item> getAllItems();
}