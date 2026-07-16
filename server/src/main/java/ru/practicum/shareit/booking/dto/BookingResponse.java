package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    @NotNull(groups = OnCreate.class)
    private Long id;

    @NotNull(groups = OnCreate.class)
    private LocalDateTime start;

    @NotNull(groups = OnCreate.class)
    private LocalDateTime end;

    private ItemDto item;

    private UserDto booker;

    private BookingStatus status;
}