package ru.practicum.shareit.item.comments.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long itemId;
    private String text;
    private String authorName;
    private LocalDateTime created;
}