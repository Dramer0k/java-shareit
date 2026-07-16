package ru.practicum.shareit.item.model.dto;

import java.time.LocalDateTime;


public interface ItemBookingDataProjection {
    Long getId();

    LocalDateTime getNextBooking();

    LocalDateTime getLastBooking();
}