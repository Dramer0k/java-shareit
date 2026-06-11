package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    Map<Long, Item> items = new HashMap<>();
    private Long index = 1L;


    @Override
    public Item addItem(Item item) {
        item.setId(index++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Long itemId, Item item) {
        return items.put(itemId, item);
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> getItems(Long userID) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId() != null)
                .filter(item -> item.getOwner().getId().equals(userID))
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsByText(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return items.values().stream()
                .filter(item -> item.getName() != null && item.getDescription() != null)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .toList();
    }

    @Override
    public List<Item> getAllItems() {
        return items.values().stream().toList();
    }
}