package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ResponseWithComment {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long owner;
    private Long request;
    private LocalDateTime nextBooking;
    private LocalDateTime lastBooking;
    private List<CommentResponse> comments;
}