package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotEnoughPermitionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemBookingDataProjection;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.model.dto.ResponseWithComment;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void addItem_success_withoutRequestId() {
        Long userId = 1L;
        User owner = new User(userId, "Owner", "owner@example.com");
        ItemDto dto = new ItemDto();
        dto.setName("Laptop");
        dto.setDescription("Good laptop");
        dto.setAvailable(true);

        Item inputItem = ItemMapper.toItem(dto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        Item savedItem = new Item();
        savedItem.setId(100L);
        savedItem.setName("Laptop");
        savedItem.setOwner(owner);

        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        Item result = itemService.addItem(userId, inputItem);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getOwner()).isEqualTo(owner);
        verify(userRepository).findById(userId);
        verify(itemRepository).save(any(Item.class));
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void addItem_success_withRequestId() {
        Long userId = 1L;
        Long requestId = 5L;
        User owner = new User(userId, "Owner", "owner@example.com");
        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Need laptop");

        ItemDto dto = new ItemDto();
        dto.setName("Laptop Pro");
        dto.setDescription("Pro version");
        dto.setAvailable(false);
        dto.setRequestId(requestId);

        Item inputItem = ItemMapper.toItem(dto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Item savedItem = new Item();
        savedItem.setId(200L);
        savedItem.setName("Laptop Pro");
        savedItem.setOwner(owner);
        savedItem.setRequest(request);

        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        Item result = itemService.addItem(userId, inputItem, requestId);

        assertThat(result.getRequest()).isEqualTo(request);
        assertThat(result.getOwner()).isEqualTo(owner);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void addItem_requestNotFound() {
        Long userId = 1L;
        Long requestId = 999L;
        User owner = new User(userId, "Owner", "owner@example.com");

        ItemDto dto = new ItemDto();
        dto.setName("Tablet");
        dto.setRequestId(requestId);

        Item inputItem = ItemMapper.toItem(dto);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.addItem(userId, inputItem, requestId));

        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_success_owner() {
        Long userId = 1L;
        Long itemId = 100L;

        User owner = new User(userId, "Owner", "o@example.com");
        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setName("Old name");
        oldItem.setDescription("Old desc");
        oldItem.setAvailable(true);
        oldItem.setOwner(owner);

        Item updateDto = new Item();
        updateDto.setName("New name");
        updateDto.setDescription("New desc");
        updateDto.setAvailable(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            return item;
        });

        Item updated = itemService.updateItem(userId, itemId, updateDto);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getDescription()).isEqualTo("New desc");
        assertThat(updated.getAvailable()).isFalse();
        assertThat(updated.getOwner()).isEqualTo(owner);
        verify(itemRepository).save(oldItem);
    }

    @Test
    void updateItem_notOwner_throws() {
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long itemId = 100L;

        User owner = new User(ownerId, "Owner", "o@example.com");
        User other = new User(otherUserId, "Other", "other@example.com");

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(other));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Item updateDto = new Item();
        updateDto.setName("Name");

        assertThrows(NotEnoughPermitionException.class, () ->
                itemService.updateItem(otherUserId, itemId, updateDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItemById_success() {
        Long itemId = 100L;
        Item item = new Item();
        item.setId(itemId);
        item.setName("Headphones");
        item.setDescription("Noise-cancelling");
        item.setAvailable(true);
        User owner = new User(1L, "Owner", "o@example.com");
        item.setOwner(owner);

        ItemBookingDataProjection projection = mock(ItemBookingDataProjection.class);
        when(projection.getId()).thenReturn(itemId);
        LocalDateTime next = LocalDateTime.of(2024, 6, 1, 12, 0);
        LocalDateTime last = LocalDateTime.of(2024, 5, 20, 10, 0);
        when(projection.getNextBooking()).thenReturn(next);
        when(projection.getLastBooking()).thenReturn(last);

        Comment c1 = new Comment();
        c1.setId(5L);
        c1.setText("Great item!");
        c1.setItem(item);
        c1.setAuthor(new User(2L, "Commenter", "c@example.com"));
        c1.setCreated(now);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findBookingDateByItem(itemId)).thenReturn(projection);
        when(commentRepository.findAllByItemId(itemId)).thenReturn(List.of(c1));

        ResponseWithComment response = itemService.getItemById(itemId);

        assertThat(response.getId()).isEqualTo(itemId);
        assertThat(response.getName()).isEqualTo("Headphones");
        assertThat(response.getNextBooking()).isEqualTo(next);
        assertThat(response.getLastBooking()).isEqualTo(last);
        assertThat(response.getComments()).hasSize(1);
        assertThat(response.getComments().getFirst().getText()).isEqualTo("Great item!");
    }

    @Test
    void getItems_success() {
        Long userId = 1L;
        User owner = new User(userId, "Owner", "o@example.com");

        Item i1 = new Item(); i1.setId(1L); i1.setName("Item1"); i1.setOwner(owner);
        Item i2 = new Item(); i2.setId(2L); i2.setName("Item2"); i2.setOwner(owner);

        ItemBookingDataProjection p1 = mock(ItemBookingDataProjection.class);
        when(p1.getId()).thenReturn(1L);
        when(p1.getNextBooking()).thenReturn(LocalDateTime.of(2024, 7, 1, 10, 0));
        when(p1.getLastBooking()).thenReturn(null);

        ItemBookingDataProjection p2 = mock(ItemBookingDataProjection.class);
        when(p2.getId()).thenReturn(2L);
        when(p2.getNextBooking()).thenReturn(null);
        when(p2.getLastBooking()).thenReturn(LocalDateTime.of(2024, 6, 20, 8, 0));

        when(bookingRepository.findItemBookingDataByOwnerId(userId))
                .thenReturn(List.of(p1, p2));

        List<Item> items = List.of(i1, i2);
        when(itemRepository.findByIdIn(anyList())).thenReturn(items);

        List<ResponseWithBookingData> responses = itemService.getItems(userId);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        assertThat(responses.get(0).getNextBooking()).isNotNull();
        assertThat(responses.get(1).getLastBooking()).isNotNull();
    }

    @Test
    void searchItems_emptyText_returnsEmpty() {
        String text = "";
        List<ItemDto> result = itemService.searchItems(text);
        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void searchItems_success() {
        String text = "laptop";
        Item i1 = new Item(); i1.setId(1L); i1.setName(text); i1.setAvailable(true);
        List<Item> found = List.of(i1);

        when(itemRepository.search(text)).thenReturn(found);

        List<ItemDto> result = itemService.searchItems(text);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo(text);
    }

    @Test
    void setComment_userDidNotRentItem_throws() {
        Long userId = 2L;
        Long itemId = 100L;
        CommentRequest commentRequest = new CommentRequest("Nice");

        Item item = new Item(); item.setId(itemId);

        User user = new User(userId, "User", "u@example.com");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(bookingRepository
                .findAllByItemIdAndBookerIdEqualsAndEndIsBeforeAndStatusEquals(
                        eq(itemId), eq(userId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of());

        assertThrows(IncorrectParamsException.class, () ->
                itemService.setComment(userId, itemId, commentRequest));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void setComment_success() {
        Long userId = 2L;
        Long itemId = 100L;
        CommentRequest commentRequest = new CommentRequest("Very good item");

        Item item = new Item(); item.setId(itemId);
        User user = new User(userId, "User", "u@example.com");

        Booking approvedBooking = new Booking();
        approvedBooking.setStatus(BookingStatus.APPROVED);
        approvedBooking.setItem(item);
        approvedBooking.setBooker(user);
        approvedBooking.setEnd(LocalDateTime.now().minusDays(1));

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAllByItemIdAndBookerIdEqualsAndEndIsBeforeAndStatusEquals(
                        eq(itemId),
                        eq(userId),
                        any(LocalDateTime.class),
                        eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(approvedBooking));

        Comment savedComment = new Comment();
        savedComment.setId(7L);
        savedComment.setText(commentRequest.getText());
        savedComment.setItem(item);
        savedComment.setAuthor(user);
        savedComment.setCreated(LocalDateTime.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse response = itemService.setComment(userId, itemId, commentRequest);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getText()).isEqualTo("Very good item");
        assertThat(response.getItemId()).isEqualTo(itemId);
        assertThat(response.getAuthorName()).isEqualTo("User");
    }
}