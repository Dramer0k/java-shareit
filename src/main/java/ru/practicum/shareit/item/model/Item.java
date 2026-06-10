package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private Long id;

    @NotBlank(message = "Name обязательное поле")
    private String name;

    @NotBlank(message = "Description обязательное поле")
    private String description;

    @NotNull(message = "Available обязательное поле")
    private Boolean available;
    private User owner;
    private ItemRequest request;
}