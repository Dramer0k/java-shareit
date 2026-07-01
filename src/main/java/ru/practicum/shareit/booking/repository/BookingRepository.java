package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemBookingDataProjection;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.item WHERE b.booker.id = ?1")
    List<Booking> findAllBookingByBookerId(Long userId);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartAsc(Long userId, Instant now);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartAsc(Long userId, Instant now);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartAsc(Long userId, Instant now, Instant nowed);

    List<Booking> findAllByBookerIdAndStatusOrderByStartAsc(Long userId, BookingStatus bookingStatus);

    List<BookingResponse> findAllByItemOwnerId(Long userId);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartAsc(Long userId, Instant now);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartAsc(Long userId, Instant now, Instant nowed);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartAsc(Long userId, Instant now);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartAsc(Long userId, BookingStatus bookingStatus);

    @Query(nativeQuery = true, value = """
            SELECT
                it.id AS id,
                MIN(CASE WHEN b.start_date >= NOW() THEN b.start_date END) AS nextBooking,
                MAX(CASE WHEN b.end_date < NOW() - INTERVAL '10' SECOND THEN b.end_date END) AS lastBooking
            FROM items it
            LEFT JOIN bookings b ON it.id = b.item_id
            WHERE it.owner_id = ?1
            GROUP BY it.id;
            """)
    List<ItemBookingDataProjection> findItemBookingDataByOwnerId(Long ownerId);

    List<Booking> findAllByItemIdAndBookerIdEqualsAndEndIsBefore(Long itemId, Long userId, LocalDateTime now);

    @Query(nativeQuery = true, value = """
            SELECT
                it.id AS id,
                MIN(CASE WHEN b.start_date >= NOW() THEN b.start_date END) AS nextBooking,
                MAX(CASE WHEN b.end_date < NOW() - INTERVAL '10' SECOND THEN b.end_date END) AS lastBooking
            FROM items it
            LEFT JOIN bookings b ON it.id = b.item_id
            WHERE it.id = ?1
            GROUP BY it.id;
""")
    ItemBookingDataProjection findBookingDateByItem(Long itemId);
}