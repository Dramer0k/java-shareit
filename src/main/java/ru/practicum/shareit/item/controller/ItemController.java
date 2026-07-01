package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.dto.ResponseWithComment;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

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
                           @RequestBody ItemDto itemDto) {
        Item item = itemService.addItem(userId, ItemMapper.toItem(itemDto));
        return ItemMapper.toDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        Item item = itemService.updateItem(userId, itemId, ItemMapper.toItem(itemDto));
        return ItemMapper.toDto(item);
    }

    @GetMapping("/{itemId}")
    public ResponseWithComment getItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ResponseWithBookingData> getItems(@RequestHeader(USER_ID) Long userId) {
        return itemService.getItems(userId);
    }

    @GetMapping("/all")
    public List<Item> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/search")
    public List<Item> getItemsByText(@RequestParam(defaultValue = "") String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponse addComment(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequest commentRequest
            ) {
        return itemService.setComment(userId, itemId, commentRequest);

    }
}