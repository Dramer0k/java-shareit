package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class IntegrationBookingServiceTest {
    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    Item item;
    User user;
    BookingRequest bookingRequest;
    Booking booking;

    @BeforeEach
    public void beforeEach() {
        user = userService.addUser(UserMapper.toUser(generateUserDto()));
        item = itemService.addItem(user.getId(), ItemMapper.toItem(generateItemDto()));
        bookingRequest = createBookingRequest(item);
        booking = BookingMapper.toBooking(bookingService.create(user.getId(), bookingRequest));

    }

    @Test
    public void createBookingTest() {
        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking result = query.setParameter("id", booking.getId()).getSingleResult();

        assertNotNull(result.getId());
        assertEquals(result.getStart(), bookingRequest.getStart());
        assertEquals(result.getEnd(), bookingRequest.getEnd());
        assertTrue(result.getEnd().isAfter(booking.getStart()));
    }

    @Test
    public void changeStatusTest() {
        bookingService.changeStatus(user.getId(), booking.getId(), true);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.id = :id", Booking.class);
        Booking result = query.setParameter("id", booking.getId()).getSingleResult();


        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }


    private ItemDto generateItemDto() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("itemName");
        itemDto.setDescription("description");
        itemDto.setAvailable(true);
        return itemDto;
    }

    private UserDto generateUserDto() {
        UserDto dto = new UserDto();
        dto.setEmail("user@email.com");
        dto.setName("user");
        return dto;
    }

    private BookingRequest createBookingRequest(Item item) {
        BookingRequest result = new BookingRequest();
        result.setItemId(item.getId());
        result.setStart(LocalDateTime.now().plusMinutes(60));
        result.setEnd(LocalDateTime.now().plusMinutes(360));
        return result;
    }


}