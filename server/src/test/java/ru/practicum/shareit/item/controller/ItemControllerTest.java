package ru.practicum.shareit.item.controller;

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
class ItemControllerTest {
    @Mock
    ItemService service;

    @InjectMocks
    ItemController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private ItemDto itemDto;

    private Item item;

    private UserDto userDto;

    private ItemRequestDto itemRequestDto;

    private ResponseWithComment responseWithComment;

    private List<ResponseWithBookingData> withBookingDataList;

    private List<ItemDto> itemDtoList;

    private CommentResponse commentResponse;

    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        userDto = new UserDto(1L, "John", "john.doe@mail.com");

        UserDto user2 = new UserDto(2L, "name", "email@gmail.com");

        ItemRequest itemRequest = new ItemRequest(
                1L,
                "description",
                UserMapper.toUser(user2),
                LocalDateTime.now());

        itemDto = new ItemDto(
                1L,
                "itemName",
                "description",
                true,
                1L,
                1L);



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
                1L,
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(1),
                Arrays.asList(
                        new CommentResponse(
                                1L,
                                1L,
                                "text",
                                "authorName",
                                LocalDateTime.now()
                        ),
                        new CommentResponse(
                                2L,
                                1L,
                                "text",
                                "authorName",
                                LocalDateTime.now()
                        )
                )
        );

        withBookingDataList = Arrays.asList(
                new ResponseWithBookingData(
                        1L,
                        "name",
                        "description",
                        true,
                        1L,
                        1L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().minusDays(1)
                ),
                new ResponseWithBookingData(
                        2L,
                        "name",
                        "description",
                        true,
                        1L,
                        1L,
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().minusDays(1)
                )
        );

        itemDtoList = Arrays.asList(
                new ItemDto(
                        1L,
                        "itemName",
                        "description",
                        true,
                        1L,
                        1L),
                new ItemDto(
                        2L,
                        "itemName",
                        "description",
                        true,
                        1L,
                        1L)
        );

        commentResponse = new CommentResponse(
                1L,
                1L,
                "text",
                "authorName",
                LocalDateTime.now()
        );

        commentRequest = new CommentRequest("commentText");

    }

    @Test
    void addItem() throws Exception {
        when(service.addItem(anyLong(), any(Item.class), anyLong())).thenReturn(item);

        mvc.perform(post("/items")
                .header("X-Sharer-User-Id", "1")
                .content(mapper.writeValueAsString(itemDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.owner", is(item.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.requestId", is(item.getRequest().getId()), Long.class))
        ;

        verify(service, times(1)).addItem(anyLong(), any(Item.class), anyLong());
    }

    @Test
    void updateItem() throws Exception {

        ItemDto updateItemDto = new ItemDto(
                1L,
                "newName",
                "newDescription",
                true,
                null,
                null);

        when(service.updateItem(anyLong(), anyLong(),any())).thenReturn(ItemMapper.toItem(updateItemDto));

        mvc.perform(patch("/items/1")
                .header("X-Sharer-User-Id", "1")
                .content(mapper.writeValueAsString(updateItemDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                 .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateItemDto.getName())))
                .andExpect(jsonPath("$.available", is(updateItemDto.getAvailable())))
                .andExpect(jsonPath("$.description", is(updateItemDto.getDescription())));

        verify(service, times(1)).updateItem(anyLong(), anyLong(),any());
    }

    @Test
    void getItemById() throws Exception {
        when(service.getItemById(anyLong())).thenReturn(responseWithComment);

        mvc.perform(get("/items/1")
                 .characterEncoding(StandardCharsets.UTF_8)
                 .contentType(MediaType.APPLICATION_JSON)
                 .accept(MediaType.APPLICATION_JSON))
                 .andDo(print())
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$.id", is(responseWithComment.getId()), Long.class))
                 .andExpect(jsonPath("$.name", is(responseWithComment.getName())))
                 .andExpect(jsonPath("$.available", is(responseWithComment.getAvailable())))
                 .andExpect(jsonPath("$.description", is(responseWithComment.getDescription())))
                 .andExpect(jsonPath("$.owner", is(responseWithComment.getOwner()), Long.class))
                 .andExpect(jsonPath("$.request", is(responseWithComment.getRequest()), Long.class))
        ;

        verify(service, times(1)).getItemById(anyLong());
    }

    @Test
    void getItems() throws Exception {
        when(service.getItems(anyLong())).thenReturn(withBookingDataList);

        mvc.perform(get("/items")
                .header("X-Sharer-User-Id", "1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(withBookingDataList.size())))
                .andExpect(jsonPath("$[0].id", is(withBookingDataList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(withBookingDataList.getFirst().getName())))
                .andExpect(jsonPath("$[0].description", is(withBookingDataList.getFirst().getDescription())))
                .andExpect(jsonPath("$[0].available", is(withBookingDataList.getFirst().getAvailable())))
                .andExpect(jsonPath("$[0].owner", is(withBookingDataList.getFirst().getOwner()), Long.class))
                .andExpect(jsonPath("$[0].request", is(withBookingDataList.getFirst().getRequest()), Long.class))
        ;

        verify(service, times(1)).getItems(anyLong());
    }

    @Test
    void getAllItems() throws Exception {
        when(service.getAllItems()).thenReturn(itemDtoList);

        mvc.perform(get("/items/all")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(itemDtoList.size())))
                .andExpect(jsonPath("$[0].id", is(itemDtoList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoList.getFirst().getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoList.getFirst().getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoList.getFirst().getAvailable())))
                .andExpect(jsonPath("$[0].owner", is(itemDtoList.getFirst().getOwner()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoList.getFirst().getRequestId()), Long.class))
        ;

        verify(service, times(1)).getAllItems();
    }

    @Test
    void getItemsByText() throws Exception {
        when(service.searchItems(anyString())).thenReturn(itemDtoList);

        mvc.perform(get("/items/search?text=description")
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(itemDtoList.size())))
                .andExpect(jsonPath("$[0].id", is(itemDtoList.getFirst().getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoList.getFirst().getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtoList.getFirst().getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDtoList.getFirst().getAvailable())))
                .andExpect(jsonPath("$[0].owner", is(itemDtoList.getFirst().getOwner()), Long.class))
                .andExpect(jsonPath("$[0].requestId", is(itemDtoList.getFirst().getRequestId()), Long.class))
                ;
    }

    @Test
    void addComment() throws Exception {
        when(service.setComment(anyLong(), anyLong(), any())).thenReturn(commentResponse);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(commentRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponse.getId()), Long.class))
                .andExpect(jsonPath("$.itemId", is(commentResponse.getItemId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentResponse.getText())))
                .andExpect(jsonPath("$.authorName", is(commentResponse.getAuthorName())))
                ;
    }
}