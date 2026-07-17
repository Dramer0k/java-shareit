package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

@Data
public class UserRequestDto {
    private Long id;

    @NotNull(groups = {OnCreate.class})
    private String name;

    @Email(groups = {OnCreate.class})
    private String email;
}