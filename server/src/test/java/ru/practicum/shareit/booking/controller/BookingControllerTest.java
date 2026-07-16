package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService service;

    @Autowired
    private ObjectMapper mapper;

    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;
    private List<BookingResponse> bookingResponseList;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        UserDto userDto = new UserDto(1L, "John", "john.doe@mail.com");
        ItemDto itemDto = new ItemDto(
                1L,
                "itemName",
                "description",
                true,
                1L,
                1L
        );

        bookingRequest = new BookingRequest(1L, start, end);

        bookingResponse = new BookingResponse(
                1L,
                start,
                end,
                itemDto,
                userDto,
                BookingStatus.WAITING
        );

        bookingResponseList = List.of(bookingResponse);
    }

    // --- Позитивные сценарии ---

    @Test
    void getBookingById_success() throws Exception {
        when(service.findBookingById(eq(1L), eq(1L))).thenReturn(bookingResponse);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(service).findBookingById(eq(1L), eq(1L));
    }

    @Test
    void getBookings_success() throws Exception {
        when(service.getBooking(eq(1L), any())).thenReturn(bookingResponseList);

        mvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")));

        verify(service).getBooking(eq(1L), any());
    }

    @Test
    void getBookingsByOwner_success() throws Exception {
        when(service.getBookingsByOwner(eq(1L), any())).thenReturn(bookingResponseList);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service).getBookingsByOwner(eq(1L), any());
    }

    @Test
    void addBooking_success() throws Exception {
        when(service.create(eq(1L), any())).thenReturn(bookingResponse);

        String content = mapper.writeValueAsString(bookingRequest);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(service).create(eq(1L), any());
    }

    @Test
    void changeStatus_success() throws Exception {
        BookingResponse updated = new BookingResponse(
                bookingResponse.getId(),
                bookingResponse.getStart(),
                bookingResponse.getEnd(),
                bookingResponse.getItem(),
                bookingResponse.getBooker(),
                BookingStatus.APPROVED
        );
        when(service.changeStatus(eq(1L), eq(1L), eq(true))).thenReturn(updated);

        mvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(service).changeStatus(eq(1L), eq(1L), eq(true));
    }

    @Test
    void getAllBookings_success() throws Exception {
        when(service.getAllBookings()).thenReturn(bookingResponseList);

        mvc.perform(get("/bookings/all")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service).getAllBookings();
    }

    @Test
    void getBookingById_notFound() throws Exception {
        when(service.findBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(service).findBookingById(anyLong(), anyLong());
    }

    @Test
    void getBookings_emptyList() throws Exception {
        long userId = 999L;
        BookingState state = BookingState.ALL;

        when(service.getBooking(eq(userId), eq(state)))
                .thenReturn(List.of());

        mvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", String.valueOf(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getBooking(eq(userId), eq(state));
    }

    @Test
    void addBooking_validationError_endBeforeStart() throws Exception {
        BookingRequest invalid = new BookingRequest(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)  // end < start
        );

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(), any());
    }

    @Test
    void addBooking_validationError_nullDates() throws Exception {
        BookingRequest invalid = new BookingRequest(1L, null, null);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}