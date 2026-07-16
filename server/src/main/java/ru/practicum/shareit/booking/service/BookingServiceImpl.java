package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponse create(Long userId, BookingRequest bookingRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingRequest.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));

        if (item.getAvailable() == false) {
            throw new IncorrectParamsException("Заявку на этот предмет создать нельзя, статус");
        }

        LocalDateTime start = bookingRequest.getStart()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime end = bookingRequest.getEnd()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime now = Instant.now()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        if (start.equals(end)
                || end.isBefore(start)
                || (start.isBefore(now) && end.isBefore(now))) {
            throw new IncorrectParamsException("Неверно выбрано время аренды");
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.WAITING);

        bookingRepository.save(booking);

        BookingResponse bd = BookingMapper.toResponse(booking);

        return bd;
    }

    @Override
    public BookingResponse changeStatus(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        boolean isBooker = (booking.getBooker() != null)
                && (booking.getBooker().getId() != null)
                && booking.getBooker().getId().equals(userId);

        boolean isOwner = (booking.getItem() != null)
                && (booking.getItem().getOwner() != null)
                && (booking.getItem().getOwner().getId() != null)
                && booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotEnoughPermitionException("Недостаточно прав для изменения статуса");
        }

        BookingStatus bookingState = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;

        booking.setStatus(bookingState);

        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        User booker = booking.getBooker();
        Item item = booking.getItem();

        if (!Objects.equals(booker.getId(), userId) && !Objects.equals(item.getOwner().getId(), (userId))) {
            throw new NotEnoughPermitionException("Недостаточно прав для просмотра");
        }

        return BookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingResponse> getBooking(Long userId, BookingState state) {
        List<Booking> result;
        switch (state) {
            case ALL -> {
                return getALlBookingsById(userId);
            }
            case PAST -> {
                result = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartAsc(userId, Instant.now());
            }
            case CURRENT -> {
                result = bookingRepository
                        .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, Instant.now(), Instant.now());
            }
            case FUTURE -> {
                result = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartAsc(userId, Instant.now());
            }
            case WAITING -> {
                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartAsc(
                        userId, BookingStatus.WAITING);
            }
            case REJECTED -> {
                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartAsc(
                        userId, BookingStatus.REJECTED);
            }
            default -> throw new IllegalArgumentException("Необрабатываемый статус: " + state.name());
        }

        return result.stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> getBookingsByOwner(Long userId, BookingState state) {
        List<Booking> result;
        switch (state) {
            case ALL -> {
                return getALlByOwnerId(userId);
            }
            case PAST -> {
                result = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartAsc(userId, Instant.now());
            }
            case CURRENT -> {
                result = bookingRepository
                        .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(userId, Instant.now(), Instant.now());
            }
            case FUTURE -> {
                result = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartAsc(userId, Instant.now());
            }
            case WAITING -> {
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartAsc(
                        userId, BookingStatus.WAITING);
            }
            case REJECTED -> {
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartAsc(
                        userId, BookingStatus.REJECTED);
            }
            default -> throw new IllegalArgumentException("Необрабатываемый статус: " + state.name());
        }

        return result.stream()
                .map(BookingMapper::toResponse)
                .toList();

    }

    @Override
    public List<BookingResponse> getAllBookings() {
        List<Booking> list = bookingRepository.findAll();
        return list.stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public List<BookingResponse> getALlBookingsById(Long userId) {
        return bookingRepository.findAllBookingByBookerId(userId).stream()
                .map(BookingMapper::toResponse).toList();
    }

    public List<BookingResponse> getALlByOwnerId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return bookingRepository.findAllByItemOwnerId(userId).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }
}