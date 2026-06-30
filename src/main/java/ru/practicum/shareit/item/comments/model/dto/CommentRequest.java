package ru.practicum.shareit.item.comments.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull
    private String text;
}