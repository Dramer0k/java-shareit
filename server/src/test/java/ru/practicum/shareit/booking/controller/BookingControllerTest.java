package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
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

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {
    @Mock
    BookingService service;

    @InjectMocks
    BookingController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private BookingRequest bookingRequest;

    private BookingResponse bookingResponse;

    private List<BookingResponse> bookingResponseList;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        bookingRequest = new BookingRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        UserDto userDto = new UserDto(1L, "John", "john.doe@mail.com");

        ItemDto itemDto = new ItemDto(
                1L,
                "itemName",
                "description",
                true,
                1L,
                1L);

        bookingResponse = new BookingResponse(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemDto,
                userDto,
                BookingStatus.WAITING
        );

        bookingResponseList = Arrays.asList(
                new BookingResponse(
                        1L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        itemDto,
                        userDto,
                        BookingStatus.WAITING),
                new BookingResponse(
                        2L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2),
                        itemDto,
                        userDto,
                        BookingStatus.WAITING)
        );
    }

    @Test
    void getBookingById() throws Exception {
        when(service.findBookingById(anyLong(), anyLong())).thenReturn(bookingResponse);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponse.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingResponse.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingResponse.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is("WAITING")))
        ;

        verify(service, times(1)).findBookingById(anyLong(), anyLong());
    }

    @Test
    void getBookings() throws Exception {
        when(service.getBooking(anyLong(), any())).thenReturn(bookingResponseList);

        mvc.perform(get("/bookings?state=ALL")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(bookingResponseList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingResponseList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseList.getFirst().getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseList.getFirst().getBooker().getId()), Long.class));

        verify(service, times(1)).getBooking(anyLong(), any());
    }

    @Test
    void getBookingsByOwner() throws Exception {
        when(service.getBookingsByOwner(anyLong(), any())).thenReturn(bookingResponseList);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(bookingResponseList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingResponseList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseList.getFirst().getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseList.getFirst().getBooker().getId()), Long.class));

        verify(service, times(1)).getBookingsByOwner(anyLong(), any());
    }

    @Test
    void addBooking() throws Exception {
        when(service.create(anyLong(), any())).thenReturn(bookingResponse);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(bookingRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponse.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingResponse.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingResponse.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is("WAITING")))
        ;
    }

    @Test
    void changeStatus() throws Exception {
        when(service.changeStatus(anyLong(), anyLong(), anyBoolean())).thenReturn(bookingResponse);

        mvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponse.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingResponse.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingResponse.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is("WAITING")))
        ;
    }

    @Test
    void getAllBookings() throws Exception {
        when(service.getAllBookings()).thenReturn(bookingResponseList);

        mvc.perform(get("/bookings/all")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(bookingResponseList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingResponseList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseList.getFirst().getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseList.getFirst().getBooker().getId()), Long.class));
        ;
    }
}