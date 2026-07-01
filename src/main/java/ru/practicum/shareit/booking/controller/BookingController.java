package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.OnCreate;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponse> getBookings(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingState state
            ) {
        return bookingService.getBooking(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponse> getBookingsByOwner(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsByOwner(userId, state);
    }

    @PostMapping
    public BookingResponse addBooking(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestBody @Validated(value = OnCreate.class) BookingRequest bookingRequest) {
        return bookingService.create(userId, bookingRequest);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponse changeStatus(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @PathVariable Long bookingId,
            @RequestParam("approved") boolean approved
    ) {
        log.info("userId: {}", userId);
        log.info("bookingId: {}", bookingId);
        log.info("approved: {}", approved);
        return bookingService.changeStatus(userId, bookingId, approved);
    }

    @GetMapping("/all")
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }


}