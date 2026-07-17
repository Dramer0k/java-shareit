package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotEnoughPermitionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    User owner;

    User booker;

    Item item;

    Booking booking;

    private static final Instant FIXED_INSTANT = Instant.parse("2027-06-15T12:00:00Z");
    private static final LocalDateTime FIXED_NOW = FIXED_INSTANT.atZone(ZoneId.systemDefault()).toLocalDateTime();
    private static final LocalDateTime NOW_TIME = LocalDateTime.now();

    @BeforeEach
    public void beforeEach() {
        owner = new User();
        owner.setId(2L);
        owner.setName("Owner");

        booker = new User();
        booker.setId(3L);
        booker.setName("Booker");

        item = new Item();
        item.setId(100L);
        item.setName("Test Item");
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(200L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(FIXED_NOW.minusHours(1));
        booking.setEnd(FIXED_NOW.plusHours(1));
    }

    @Test
    void create_success() {
        Long userId = 1L;
        Long itemId = 2L;
        Long bookingId = 3L;

        User user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@example.com");

        item.setId(itemId);
        item.setAvailable(true);

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(FIXED_NOW.plusHours(1));
        req.setEnd(FIXED_NOW.plusHours(3));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.of(item));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking b = invocation.getArgument(0);
                    if (b.getId() == null) {
                        b.setId(bookingId);
                    }
                    return b;
                });

        BookingResponse response = bookingService.create(userId, req);

        assertThat(response.getId()).isEqualTo(bookingId);
        assertThat(response.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_userNotFound() {
        Long userId = 1L;
        Long itemId = 2L;

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(FIXED_NOW.plusHours(1));
        req.setEnd(FIXED_NOW.plusHours(2));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(userId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(itemRepository, Mockito.never()).findById(any());
        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void create_itemNotFound() {
        Long userId = 1L;
        Long itemId = 2L;

        User user = new User();
        user.setId(userId);

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(FIXED_NOW.plusHours(1));
        req.setEnd(FIXED_NOW.plusHours(2));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(userId, req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Предмет не найден");

        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void create_itemNotAvailable() {
        Long userId = 1L;
        Long itemId = 2L;

        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(false);

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(FIXED_NOW.plusHours(1));
        req.setEnd(FIXED_NOW.plusHours(2));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(userId, req))
                .isInstanceOf(IncorrectParamsException.class);

        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void create_startEqualsEnd() {
        Long userId = 1L;
        Long itemId = 2L;

        User user = new User(); user.setId(userId);
        Item item = new Item(); item.setId(itemId); item.setAvailable(true);

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(FIXED_NOW);
        req.setEnd(FIXED_NOW);

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(userId, req))
                .isInstanceOf(IncorrectParamsException.class);

        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void create_endBeforeStart() {
        Long userId = 1L;
        Long itemId = 2L;

        User user = new User(); user.setId(userId);
        Item item = new Item(); item.setId(itemId); item.setAvailable(true);

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(FIXED_NOW.plusHours(2));
        req.setEnd(FIXED_NOW.plusHours(1));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(userId, req))
                .isInstanceOf(IncorrectParamsException.class);

        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void create_bothTimesInPast() {
        Long userId = 1L;
        Long itemId = 2L;

        User user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@example.com");

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(user);
        item.setName("Test Item");
        item.setDescription("Description");
        item.setAvailable(true);

        BookingRequest req = new BookingRequest();
        req.setItemId(itemId);
        req.setStart(NOW_TIME.minusHours(2));
        req.setEnd(NOW_TIME.minusHours(1));

        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));
        when(itemRepository.findById(eq(itemId))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(userId, req))
                .isInstanceOf(IncorrectParamsException.class);

        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void changeStatus_success_owner() {
        Long ownerId = 4L;
        Long bookingId = 5L;

        owner.setId(ownerId);
        item.setOwner(owner);
        booking.setId(bookingId);

        when(bookingRepository.findById(eq(bookingId))).thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(999L);
            }
            return saved;
        });

        BookingResponse response = bookingService.changeStatus(ownerId, bookingId, true);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.APPROVED);

        verify(bookingRepository).save(eq(booking));
    }


    @Test
    void changeStatus_success_booker() {
        Long bookerId = 6L;
        Long bookingId = 7L;

        User booker = new User();
        booker.setId(bookerId);

        booking.setId(bookingId);
        booking.setBooker(booker);

        when(bookingRepository.findById(eq(bookingId))).thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(999L);
            }
            return saved;
        });

        var response = bookingService.changeStatus(bookerId, bookingId, false);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).save(eq(booking));
    }

    @Test
    void changeStatus_noPermission() {
        Long otherId = 99L;
        Long bookingId = 88L;

        User owner = new User(); owner.setId(10L);
        User booker = new User(); booker.setId(20L);
        Item item = new Item(); item.setOwner(owner);
        Booking booking = new Booking(); booking.setId(bookingId); booking.setItem(item); booking.setBooker(booker);

        when(bookingRepository.findById(eq(bookingId))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.changeStatus(otherId, bookingId, true))
                .isInstanceOf(NotEnoughPermitionException.class);

        verify(bookingRepository, Mockito.never()).save(any());
    }

    @Test
    void findBookingById_success_booker() {
        Long bookerId = 11L;
        Long bookingId = 12L;

        User booker = new User(); booker.setId(bookerId);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);

        when(bookingRepository.findById(eq(bookingId))).thenReturn(Optional.of(booking));

        var response = bookingService.findBookingById(bookerId, bookingId);

        assertThat(response.getId()).isEqualTo(bookingId);
        verify(bookingRepository).findById(eq(bookingId));
    }

    @Test
    void findBookingById_noAccess() {
        Long otherId = 88L;
        Long bookingId = 99L;

        User owner = new User(); owner.setId(77L);
        User booker = new User(); booker.setId(66L);
        Item item = new Item(); item.setOwner(owner);
        Booking booking = new Booking(); booking.setId(bookingId); booking.setItem(item); booking.setBooker(booker);

        when(bookingRepository.findById(eq(bookingId))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.findBookingById(otherId, bookingId))
                .isInstanceOf(NotEnoughPermitionException.class);

        verify(bookingRepository).findById(eq(bookingId));
    }

    @Test
    void getBooking_all() {
        Long userId = 1L;
        List<Booking> bookings = List.of(booking);

        when(bookingRepository.findAllBookingByBookerId(eq(userId))).thenReturn(bookings);

        var result = bookingService.getBooking(userId, BookingState.ALL);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllBookingByBookerId(eq(userId));
    }

    @Test
    void getBooking_past() {
        Long userId = 1L;
        List<Booking> bookings = List.of(booking);

        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartAsc(eq(userId), any(Instant.class)))
                .thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBooking(userId, BookingState.PAST);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBooking_current() {
        Long userId = 1L;
        List<Booking> bookings = List.of(booking);

        when(bookingRepository
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(eq(userId), any(Instant.class), any(Instant.class)))
                .thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBooking(userId, BookingState.CURRENT);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBooking_future() {
        Long userId = 1L;
        List<Booking> bookings = List.of(booking);

        when(bookingRepository
                .findAllByBookerIdAndStartAfterOrderByStartAsc(eq(userId), any(Instant.class)))
                .thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBooking(userId, BookingState.FUTURE);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBooking_waiting() {
        Long userId = 1L;
        List<Booking> bookings = List.of(booking);

        when(bookingRepository
                .findAllByBookerIdAndStatusOrderByStartAsc(eq(userId), any(BookingStatus.class)))
                .thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBooking(userId, BookingState.WAITING);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBooking_reject() {
        Long userId = 1L;
        List<Booking> bookings = List.of(booking);

        when(bookingRepository
                .findAllByBookerIdAndStatusOrderByStartAsc(eq(userId), any(BookingStatus.class)))
                .thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBooking(userId, BookingState.REJECTED);

        assertThat(result).hasSize(1);
    }


    @Test
    void getBookingsByOwner_current() {
        Long ownerId = 2L;
        Long bookerId = 3L;

        List<Booking> bookings = List.of(booking);

        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(
                eq(ownerId), any(Instant.class), any(Instant.class)
        )).thenReturn(bookings);

        List<BookingResponse> result = bookingService.getBookingsByOwner(ownerId, BookingState.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
        assertThat(result.getFirst().getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getFirst().getBooker().getId()).isEqualTo(booker.getId());
    }

}