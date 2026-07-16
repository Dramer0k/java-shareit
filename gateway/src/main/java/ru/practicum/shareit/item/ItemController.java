package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.validation.OnCreate;

@Controller
@RequestMapping(path = "/items")
@AllArgsConstructor
@Slf4j
@Validated
public class ItemController {
    public static final String USER_ID = "X-Sharer-User-Id";
    private ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(USER_ID) Long userId,
                                          @RequestBody @Validated(value = OnCreate.class) ItemRequestDto itemDto) {
        log.info("POST /items");
        log.info("ItemDto: {}", itemDto);
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemRequestDto itemDto) {
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId) {

        log.info("GET /items/{itemId}");
        return itemClient.getItemById(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(USER_ID) Long userId) {
        log.info("GET /items");
        return itemClient.getItems(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItems() {
        log.info("GET /items/all");
        return itemClient.getAllItems();
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByText(@RequestParam(defaultValue = "") String text) {
        log.info("GET /items/search");
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequest
    ) {
        log.info("POST /items/{itemId}/comment");
        return itemClient.setComment(userId, itemId, commentRequest);
    }
}