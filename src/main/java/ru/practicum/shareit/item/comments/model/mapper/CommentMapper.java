package ru.practicum.shareit.item.comments.model.mapper;

import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(User user, Item item, CommentRequest commentRequest) {
        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    public static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getItem().getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

}