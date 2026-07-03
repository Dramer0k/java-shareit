package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    WAITING, //бронирование подтверждено владельцем
    APPROVED, // бронирование подтверждено владельцем
    REJECTED, //бронирование отклонено владельцем
    CANCELED //бронирование отменено создателем
}