package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class ItemRequestController {
    private ItemRequestService itemRequestService;
    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestResponse addRequest(
            @RequestHeader(USER_ID) Long userId,
            @RequestBody ItemRequestDto itemRequestDto
            ) {
        log.info("POST /requests");
        return itemRequestService.addRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithAnswer> getRequestsByUser(
            @RequestHeader(USER_ID) Long userId
    ) {
        log.info("GET /requests");
        return itemRequestService.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponse> getAllRequest(
            @RequestHeader(USER_ID) Long userId
    ) {
        log.info("GET /requests/all");
        return itemRequestService.getAllRequest(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithAnswer findById(@PathVariable Long requestId,
                                        @RequestHeader(USER_ID) Long userId) {
        log.info("GET /requests/{requestId}");
        return itemRequestService.findById(requestId);
    }
}