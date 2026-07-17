package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.model.dto.ItemInRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestService service;

    @Autowired
    private ObjectMapper mapper;

    private UserDto userDto;
    private ItemRequestDto itemRequestDto;
    private ItemRequestResponse itemRequestResponse;
    private ItemRequestWithAnswer itemRequestWithAnswer;
    private List<ItemRequestWithAnswer> withAnswerList;
    private List<ItemRequestResponse> requestResponseList;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "John", "john.doe@mail.com");

        itemRequestDto = new ItemRequestDto("description");

        LocalDateTime now = LocalDateTime.now();
        itemRequestResponse = new ItemRequestResponse(
                1L,
                "description",
                UserMapper.toUser(userDto),
                now
        );

        itemRequestWithAnswer = new ItemRequestWithAnswer(
                1L,
                "description",
                now,
                List.of(
                        new ItemInRequest(1L, "Joze", 1L),
                        new ItemInRequest(2L, "Goha", 1L)
                )
        );

        withAnswerList = List.of(
                new ItemRequestWithAnswer(
                        1L,
                        "description",
                        now,
                        List.of(
                                new ItemInRequest(1L, "Joze", 1L),
                                new ItemInRequest(2L, "Goha", 1L)
                        )),
                new ItemRequestWithAnswer(
                        2L,
                        "description",
                        now.plusHours(1),
                        List.of(
                                new ItemInRequest(1L, "Joze", 1L),
                                new ItemInRequest(2L, "Goha", 1L)
                        ))
        );

        requestResponseList = List.of(
                new ItemRequestResponse(
                        1L,
                        "description",
                        UserMapper.toUser(userDto),
                        now),
                new ItemRequestResponse(
                        2L,
                        "another description",
                        UserMapper.toUser(userDto),
                        now.minusDays(1))
        );
    }

    @Test
    void addRequest_success() throws Exception {
        ItemRequestDto dto = new ItemRequestDto("new description");
        when(service.addRequest(eq(1L), any()))
                .thenReturn(itemRequestResponse);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.requestor.id", is(1)));

        verify(service).addRequest(eq(1L), any());
    }

    @Test
    void getRequestsByUser_success() throws Exception {
        when(service.getRequestsByRequestor(eq(1L))).thenReturn(withAnswerList);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(service).getRequestsByRequestor(eq(1L));
    }

    @Test
    void getAllRequests_success() throws Exception {
        when(service.getAllRequest(eq(1L))).thenReturn(requestResponseList);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(service).getAllRequest(eq(1L));
    }

    @Test
    void findById_success() throws Exception {
        when(service.findById(eq(1L))).thenReturn(itemRequestWithAnswer);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.items", hasSize(2)));

        verify(service).findById(eq(1L));
    }

    @Test
    void addRequest_validationError_emptyDescription() throws Exception {
        ItemRequestDto invalid = new ItemRequestDto("");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(service, never()).addRequest(any(), any());
    }

    @Test
    void addRequest_validationError_nullDescription() throws Exception {
        ItemRequestDto invalid = new ItemRequestDto(null);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsByUser_emptyList() throws Exception {
        when(service.getRequestsByRequestor(anyLong())).thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getRequestsByRequestor(eq(999L));
    }

    @Test
    void findById_notFound() throws Exception {
        when(service.findById(anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;

        verify(service).findById(eq(999L));
    }
}