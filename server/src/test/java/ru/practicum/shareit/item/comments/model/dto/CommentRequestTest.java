package ru.practicum.shareit.item.comments.model.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRequestTest {
    private final JacksonTester<CommentRequest> json;

    @Test
    void textCommentRequest() throws Exception {
        CommentRequest commentRequest = new CommentRequest("description");

        JsonContent<CommentRequest> result = json.write(commentRequest);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("description");
    }

}