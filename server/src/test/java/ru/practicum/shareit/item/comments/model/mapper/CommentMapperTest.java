package ru.practicum.shareit.item.comments.model.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {
    private final Long userId = 1L;
    private final Long itemId = 2L;
    private final User author = new User(userId, "Maria", "maria@example.com");

    @Test
    void toComment_success() {
        String text = "Отличный предмет, всё понравилось!";
        LocalDateTime now = LocalDateTime.now();

        Item item = new Item(itemId, "Drill", "Cordless drill", true, author, null);
        CommentRequest commentRequest = new CommentRequest(text);

        Comment comment = CommentMapper.toComment(author, item, commentRequest);

        assertThat(comment.getId()).isNull();
        assertThat(comment.getText()).isEqualTo(text);
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreated()).isNotNull();
        assertThat(comment.getCreated())
                .isBetween(now.minusSeconds(5), now.plusSeconds(5));
    }

    @Test
    void toComment_withNullText() {
        Item item = new Item(itemId, "Laptop", "Good laptop", true, author, null);
        CommentRequest commentRequest = new CommentRequest(null);

        Comment comment = CommentMapper.toComment(author, item, commentRequest);

        assertThat(comment.getText()).isNull();
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void toResponse_success() {
        Long commentId = 100L;
        Long itemId = 200L;
        Long authorId = 300L;
        String text = "Работает отлично, спасибо!";
        String authorName = "Ivan";
        LocalDateTime created = LocalDateTime.of(2024, 5, 10, 14, 30);

        User author = new User(authorId, authorName, "ivan@example.com");
        Item item = new Item(itemId, "Headphones", "Noise-cancelling", true, author, null);


        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(created);

        CommentResponse response = CommentMapper.toResponse(comment);

        assertThat(response.getId()).isEqualTo(commentId);
        assertThat(response.getItemId()).isEqualTo(itemId);
        assertThat(response.getText()).isEqualTo(text);
        assertThat(response.getAuthorName()).isEqualTo(authorName);
        assertThat(response.getCreated()).isEqualTo(created);
    }

    @Test
    void toResponse_withNullFields() {
        Comment comment = new Comment();
        comment.setId(999L);
        comment.setText(null);
        comment.setItem(null);
        comment.setAuthor(null);
        comment.setCreated(null);

        CommentResponse response = CommentMapper.toResponse(comment);

        assertThat(response.getId()).isEqualTo(999L);
        assertThat(response.getItemId()).isNull();
        assertThat(response.getText()).isNull();
        assertThat(response.getAuthorName()).isNull();
        assertThat(response.getCreated()).isNull();
    }
}