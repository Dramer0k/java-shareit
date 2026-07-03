package ru.practicum.shareit.booking.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

@Slf4j
@Component
public class BookingMapper {
    public static BookingResponse toResponse(Booking booking) {
        log.info("{}", booking);
        return new BookingResponse(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new ItemDto(booking.getItem().getId(),
                        booking.getItem().getName(),
                        booking.getItem().getDescription(),
                        booking.getItem().getAvailable(),
                        booking.getItem().getOwner().getId(),
                        booking.getItem().getRequest() != null ? booking.getItem().getRequest().getId() : null),
                new UserDto(booking.getBooker().getId(),
                        booking.getBooker().getName(),
                        booking.getBooker().getEmail()),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingResponse bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(ItemMapper.toItem(bookingDto.getItem()));
        booking.setBooker(UserMapper.toUser(bookingDto.getBooker()));
        booking.setStatus(bookingDto.getStatus());

        return booking;
    }
}