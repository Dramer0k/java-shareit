package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ResponseWithBookingData {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long owner;
    private Long request;
    private LocalDateTime nextBooking;
    private LocalDateTime lastBooking;

}