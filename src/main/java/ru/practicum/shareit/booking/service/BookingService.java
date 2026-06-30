package ru.practicum.shareit.booking.service;

import jakarta.validation.Valid;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponse create(Long userId, @Valid BookingRequest bookingRequest);

    BookingResponse changeStatus(Long userId, Long bookingId, boolean approved);

    BookingResponse findBookingById(Long userId, Long bookingId);

    List<BookingResponse> getBooking(Long userId, BookingState state);

    List<BookingResponse> getBookingsByOwner(Long userId, BookingState state);
}