package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.model.dto.ResponseWithComment;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.OnCreate;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private ItemService itemService;
    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto addItem(@RequestHeader(USER_ID) Long userId,
                           @RequestBody @Validated(value = OnCreate.class) ItemDto itemDto) {
        log.info("POST /items");
        log.info("Добавляем итем: {}", itemDto);
        log.info("Пользователь: {}", userId);

        Item item;
        if (itemDto.getRequestId() != null) {
            item = itemService.addItem(userId, ItemMapper.toItem(itemDto), itemDto.getRequestId());
        } else {
            item = itemService.addItem(userId, ItemMapper.toItem(itemDto));
        }

        log.info("Итем после апдейта: {}", itemDto);

        return ItemMapper.toDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{itemId}");
        Item item = itemService.updateItem(userId, itemId, ItemMapper.toItem(itemDto));
        return ItemMapper.toDto(item);
    }

    @GetMapping("/{itemId}")
    public ResponseWithComment getItemById(@PathVariable Long itemId) {

        log.info("GET /items/{itemId}");
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ResponseWithBookingData> getItems(@RequestHeader(USER_ID) Long userId) {
        log.info("GET /items");
        return itemService.getItems(userId);
    }

    @GetMapping("/all")
    public List<ItemDto> getAllItems() {
        log.info("GET /items/all");
        return itemService.getAllItems();
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByText(@RequestParam(defaultValue = "") String text) {
        log.info("GET /items/search");
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse addComment(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequest commentRequest
            ) {
        log.info("POST /items/{itemId}/comment");
        return itemService.setComment(userId, itemId, commentRequest);
    }
}