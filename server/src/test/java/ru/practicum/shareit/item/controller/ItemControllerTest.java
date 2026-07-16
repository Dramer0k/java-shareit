package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import ru.practicum.shareit.exception.GlobalExceptionHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.model.dto.ResponseWithComment;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    private MockMvc mvc;

    @Mock
    private ItemService service;

    @InjectMocks
    private ItemController controller;

    private final ObjectMapper mapper = new ObjectMapper();
    private UserDto userDto;
    private UserDto user2;
    private ItemRequest itemRequest;
    private ItemDto itemDto;
    private Item item;
    private ResponseWithComment responseWithComment;
    private List<ResponseWithBookingData> withBookingDataList;
    private List<ItemDto> itemDtoList;
    private CommentResponse commentResponse;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();

        userDto = new UserDto(1L, "John", "john.doe@mail.com");
        user2 = new UserDto(2L, "name", "email@gmail.com");

        itemRequest = new ItemRequest(
                1L,
                "description",
                UserMapper.toUser(user2),
                LocalDateTime.now()
        );

        itemDto = new ItemDto(
                1L,
                "itemName",
                "description",
                true,
                1L,
                1L
        );

        item = new Item(
                1L,
                "newName",
                "description",
                true,
                UserMapper.toUser(userDto),
                itemRequest
        );

        responseWithComment = new ResponseWithComment(
                1L,
                "name",
                "description",
                true,
                1L, 1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1),
                List.of(
                        new CommentResponse(1L, 1L, "text", "authorName", LocalDateTime.now()),
                        new CommentResponse(2L, 1L, "text", "authorName", LocalDateTime.now())
                )
        );

        withBookingDataList = List.of(
                new ResponseWithBookingData(1L, "name", "description", true, 1L, 1L,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1)),
                new ResponseWithBookingData(2L, "name", "description", true, 1L, 1L,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1))
        );

        itemDtoList = List.of(
                new ItemDto(1L, "itemName", "description", true, 1L, 1L),
                new ItemDto(2L, "itemName", "description", true, 1L, 1L)
        );

        commentResponse = new CommentResponse(
                1L, 1L, "commentText", "authorName", LocalDateTime.now());
        commentRequest = new CommentRequest("commentText");
    }

    @Test
    void addItem_success_withoutRequestId() throws Exception {
        ItemDto dto = new ItemDto(null, "newItem", "desc", true, 1L, null);
        item = new Item(
                1L,
                "newItem",
                "desc",
                true,
                UserMapper.toUser(userDto),
                null
        );

        when(service.addItem(anyLong(), any(Item.class)))
                .thenReturn(item);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("newItem")))
                .andExpect(jsonPath("$.owner", is(1L), Long.class))
                .andExpect(jsonPath("$.requestId", is(nullValue())));

        verify(service).addItem(eq(1L), any(Item.class));
        verify(service, never()).addItem(anyLong(), any(Item.class), anyLong());
    }

    @Test
    void addItem_success_withRequestId() throws Exception {
        ItemDto dto = new ItemDto(null, "newName", "desc", true, 1L, 1L);

        when(service.addItem(anyLong(), any(Item.class), anyLong()))
                .thenReturn(item);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("newName")))
                .andExpect(jsonPath("$.owner", is(1L), Long.class))
                .andExpect(jsonPath("$.requestId", is(1L), Long.class));

        verify(service).addItem(eq(1L), any(Item.class), eq(1L));
    }

    @Test
    void updateItem_success() throws Exception {
        ItemDto dto = new ItemDto(1L, "updatedName", "updatedDesc", false, null, null);
        Item updatedItem = ItemMapper.toItem(dto);
        updatedItem.setOwner(UserMapper.toUser(userDto));
        updatedItem.setRequest(itemRequest);

        when(service.updateItem(anyLong(), anyLong(), any(Item.class)))
                .thenReturn(updatedItem);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("updatedName")))
                .andExpect(jsonPath("$.available", is(false)));

        verify(service).updateItem(eq(1L), eq(1L), any(Item.class));
    }

    @Test
    void getItemById_success() throws Exception {
        when(service.getItemById(anyLong())).thenReturn(responseWithComment);

        mvc.perform(get("/items/1").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.owner", is(1)))
                .andExpect(jsonPath("$.request", is(1)));

        verify(service).getItemById(eq(1L));
    }

    @Test
    void getItems_success() throws Exception {
        when(service.getItems(anyLong())).thenReturn(withBookingDataList);

        mvc.perform(get("/items").header("X-Sharer-User-Id", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(service).getItems(eq(1L));
    }

    @Test
    void getAllItems_success() throws Exception {
        when(service.getAllItems()).thenReturn(itemDtoList);

        mvc.perform(get("/items/all").header("X-Sharer-User-Id", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(service).getAllItems();
    }

    @Test
    void searchItems_withText_success() throws Exception {
        when(service.searchItems(anyString())).thenReturn(itemDtoList);

        mvc.perform(get("/items/search?text=keyword").header("X-Sharer-User-Id", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(service).searchItems(eq("keyword"));
    }

    @Test
    void searchItems_emptyText_usesDefault() throws Exception {
        when(service.searchItems(anyString())).thenReturn(List.of());

        mvc.perform(get("/items/search").header("X-Sharer-User-Id", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).searchItems(eq(""));
    }

    @Test
    void addComment_success() throws Exception {
        when(service.setComment(anyLong(), anyLong(), any()))
                .thenReturn(commentResponse);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("commentText")));

        verify(service).setComment(eq(2L), eq(1L), any());
    }

    @Test
    void addItem_validationError_nameMissing() throws Exception {
        ItemDto dto = new ItemDto(null, null, "desc", true, 1L, null);

        mvc.perform(post("/items")
                        .header("X-Sharer-UserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(service, never()).addItem(any(), any(), any());
    }

    @Test
    void addItem_validationError_descriptionMissing() throws Exception {
        ItemDto dto = new ItemDto(null, "name", null, true, 1L, null);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_notFound() throws Exception {
        when(service.getItemById(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid item"));

        mvc.perform(get("/items/999")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(service).getItemById(eq(999L));
    }

    @Test
    void addComment_invalidUserId() throws Exception {
        when(service.setComment(anyLong(), anyLong(), any()))
                .thenThrow(new NotFoundException("Invalid user"));

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(service).setComment(eq(1L), eq(1L), any());
    }
}