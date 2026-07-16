package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

@Controller
@RequestMapping(path = "/requests")
@AllArgsConstructor
@Slf4j
@Validated
public class RequestController {
    RequestClient requestClient;
    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addRequest(
            @RequestHeader(USER_ID) Long userId,
            @RequestBody RequestDto itemRequestDto
    ) {
        log.info("POST /requests");
        return requestClient.addRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByUser(
            @RequestHeader(USER_ID) Long userId
    ) {
        log.info("GET /requests");
        return requestClient.getRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequest(
            @RequestHeader(USER_ID) Long userId
    ) {
        log.info("GET /requests/all");
        return requestClient.getAllRequest(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@PathVariable Long requestId,
                                          @RequestHeader(USER_ID) Long userId) {
        log.info("GET /requests/{requestId}");
        return requestClient.findById(requestId, userId);
    }
}