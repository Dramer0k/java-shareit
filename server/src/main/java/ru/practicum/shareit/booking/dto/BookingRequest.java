package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.OnCreate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    @NotNull(groups = OnCreate.class)
    private Long itemId;

    @NotNull(groups = OnCreate.class)
    private LocalDateTime start;

    @NotNull(groups = OnCreate.class)
    private LocalDateTime end;
}