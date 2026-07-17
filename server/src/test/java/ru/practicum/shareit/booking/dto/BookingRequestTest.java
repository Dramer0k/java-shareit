package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRequestTest {
    private final JacksonTester<BookingRequest> json;

    @Test
    void textBookingRequest() throws Exception {
        BookingRequest bookingRequest = new BookingRequest(
                1L,
                LocalDateTime.parse("2026-07-20T19:00:00"),
                LocalDateTime.parse("2026-07-20T20:00:00")
        );

        JsonContent<BookingRequest> result = json.write(bookingRequest);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-07-20T19:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-07-20T20:00:00");
    }
}