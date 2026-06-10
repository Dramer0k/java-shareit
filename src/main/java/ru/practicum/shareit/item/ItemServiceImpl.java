package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotEnoughPermitionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private ItemRepository itemRepository;
    private UserRepository userRepository;

    @Override
    public Item addItem(Long userId, Item item) {
        User owner = getUser(userId);
        item.setOwner(owner);

        if (item.getName() == null || item.getName().isBlank()) {
            throw new IncorrectParamsException("Name - обязательное поле");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IncorrectParamsException("Description - обязательное поле");
        }

        if ((item.getAvailable() == null)) {
            throw new IncorrectParamsException("Available - обязательное поле");
        }

        log.info("Available: {}", item.getAvailable());
        return itemRepository.addItem(item);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) {
        User owner = getUser(userId);
        Item oldItem = getItem(itemId);

        if (!owner.getId().equals(oldItem.getOwner().getId())) {
            throw new NotEnoughPermitionException("Недостаточно прав для изменения!");
        }

        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != oldItem.getAvailable()) {
            oldItem.setAvailable(item.getAvailable());
        }

        return itemRepository.updateItem(itemId, oldItem);
    }

    @Override
    public Item getItemById(Long itemId) {
        return getItem(itemId);
    }

    @Override
    public List<Item> getItems(Long userId) {
        return itemRepository.getItems(userId);
    }

    @Override
    public List<Item> getItemsByText(String text) {
        log.info("Начался поиск по тексту: {}", text);
        return itemRepository.getItemsByText(text);
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepository.getAllItems();
    }

    public User getUser(Long userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public Item getItem(Long itemId) {
        return itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена!"));
    }
}