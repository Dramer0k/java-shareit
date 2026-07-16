package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.request.model.dto.ItemInRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    @Mock
    ItemRequestService service;

    @InjectMocks
    ItemRequestController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    UserDto userDto;

    ItemRequestDto itemRequestDto;

    ItemRequestResponse itemRequestResponse;

    ItemRequestWithAnswer itemRequestWithAnswer;

    List<ItemRequestWithAnswer> withAnswerList;

    List<ItemRequestResponse> requestResponseList;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        userDto = new UserDto(1L, "John", "john.doe@mail.com");

        itemRequestDto = new ItemRequestDto("description");

        itemRequestResponse = new ItemRequestResponse(
                1L,
                "description",
                UserMapper.toUser(userDto),
                LocalDateTime.now());

        itemRequestWithAnswer = new ItemRequestWithAnswer(
                1L,
                "description",
                LocalDateTime.now(),
                Arrays.asList(
                        new ItemInRequest(
                                1L,
                                "Joze",
                                1L),
                        new ItemInRequest(
                                2L,
                                "Goha",
                                1L)
                ));

        withAnswerList = Arrays.asList(
                new ItemRequestWithAnswer(
                        1L,
                        "description",
                        LocalDateTime.now(),
                        Arrays.asList(
                                new ItemInRequest(
                                        1L,
                                        "Joze",
                                        1L),
                                new ItemInRequest(
                                        2L,
                                        "Goha",
                                        1L)
                        )),
                new ItemRequestWithAnswer(
                        2L,
                        "description",
                        LocalDateTime.now(),
                        Arrays.asList(
                                new ItemInRequest(
                                        1L,
                                        "Joze",
                                        1L),
                                new ItemInRequest(
                                        2L,
                                        "Goha",
                                        1L)
                        ))
        );

        requestResponseList = Arrays.asList(
                new ItemRequestResponse(
                        1L,
                        "description",
                        UserMapper.toUser(userDto),
                        LocalDateTime.now()),
                new ItemRequestResponse(
                        2L,
                        "description",
                        UserMapper.toUser(userDto),
                        LocalDateTime.now())
        );
    }

    @Test
    void addRequest() throws Exception {
        when(service.addRequest(any(Long.class), any())).thenReturn(itemRequestResponse);

        mvc.perform(post("/requests")
                .header("X-Sharer-User-Id", "1")
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponse.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestResponse.getDescription())))
                .andExpect(jsonPath("$.requestor.id", is(itemRequestResponse.getRequestor().getId()), Long.class))
                .andExpect(jsonPath("$.created[0]", is(itemRequestResponse.getCreated().getYear())))
                .andExpect(jsonPath("$.created[1]", is(itemRequestResponse.getCreated().getMonthValue())))
                .andExpect(jsonPath("$.created[2]", is(itemRequestResponse.getCreated().getDayOfMonth())))
                .andExpect(jsonPath("$.created[3]", is(itemRequestResponse.getCreated().getHour())))
                .andExpect(jsonPath("$.created[4]", is(itemRequestResponse.getCreated().getMinute())))
                .andExpect(jsonPath("$.created[5]", is(itemRequestResponse.getCreated().getSecond())))
        ;

        verify(service, times(1)).addRequest(any(Long.class), any());

    }

    @Test
    void getRequestsByUser() throws Exception {
        when(service.getRequestsByRequestor(any(Long.class))).thenReturn(withAnswerList);

        mvc.perform(get("/requests")
                .header("X-Sharer-User-Id", "1")
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(withAnswerList.size())))
                .andExpect(jsonPath("$[0].id", is(withAnswerList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(withAnswerList.getFirst().getDescription())))
                .andExpect(jsonPath("$[0].created[0]", is(withAnswerList.getFirst().getCreated().getYear())))
                .andExpect(jsonPath("$[0].created[1]", is(withAnswerList.getFirst().getCreated().getMonthValue())))
                .andExpect(jsonPath("$[0].created[2]", is(withAnswerList.getFirst().getCreated().getDayOfMonth())))
                .andExpect(jsonPath("$[0].created[3]", is(withAnswerList.getFirst().getCreated().getHour())))
                .andExpect(jsonPath("$[0].created[4]", is(withAnswerList.getFirst().getCreated().getMinute())))
                .andExpect(jsonPath("$[0].created[5]", is(withAnswerList.getFirst().getCreated().getSecond())));

        verify(service, times(1)).getRequestsByRequestor(any(Long.class));
    }

    @Test
    void getAllRequest() throws Exception {
        when(service.getAllRequest(any(Long.class))).thenReturn(requestResponseList);

        mvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", "1")
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(requestResponseList.size())))
                .andExpect(jsonPath("$[0].id", is(requestResponseList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestResponseList.getFirst().getDescription())))
                .andExpect(jsonPath("$[0].created[0]", is(requestResponseList.getFirst().getCreated().getYear())))
                .andExpect(jsonPath("$[0].created[1]", is(requestResponseList.getFirst().getCreated().getMonthValue())))
                .andExpect(jsonPath("$[0].created[2]", is(requestResponseList.getFirst().getCreated().getDayOfMonth())))
                .andExpect(jsonPath("$[0].created[3]", is(requestResponseList.getFirst().getCreated().getHour())))
                .andExpect(jsonPath("$[0].created[4]", is(requestResponseList.getFirst().getCreated().getMinute())))
                .andExpect(jsonPath("$[0].created[5]", is(requestResponseList.getFirst().getCreated().getSecond())));

        verify(service, times(1)).getAllRequest(any(Long.class));
    }

    @Test
    void findById() throws Exception {
        when(service.findById(any(Long.class))).thenReturn(itemRequestWithAnswer);

        mvc.perform(get("/requests/1")
                .header("X-Sharer-User-Id", "1")
                .content(mapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestWithAnswer.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestWithAnswer.getDescription())))
                .andExpect(jsonPath("$.created[0]", is(itemRequestWithAnswer.getCreated().getYear())))
                .andExpect(jsonPath("$.created[1]", is(itemRequestWithAnswer.getCreated().getMonthValue())))
                .andExpect(jsonPath("$.created[2]", is(itemRequestWithAnswer.getCreated().getDayOfMonth())))
                .andExpect(jsonPath("$.created[3]", is(itemRequestWithAnswer.getCreated().getHour())))
                .andExpect(jsonPath("$.created[4]", is(itemRequestWithAnswer.getCreated().getMinute())))
                .andExpect(jsonPath("$.created[5]", is(itemRequestWithAnswer.getCreated().getSecond())))
                .andExpect(jsonPath("$.items", hasSize(itemRequestWithAnswer.getItems().size())));

        verify(service, times(1)).findById(any(Long.class));
    }
}