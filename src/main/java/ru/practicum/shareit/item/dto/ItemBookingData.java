package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ItemBookingData {
    private Long id;
    private LocalDateTime nextBooking;
    private LocalDateTime lastBooking;
}