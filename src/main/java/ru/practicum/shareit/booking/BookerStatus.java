package ru.practicum.shareit.booking;

public enum BookerStatus {
    WAITING, //бронирование подтверждено владельцем
    APPROVED, // бронирование подтверждено владельцем
    REJECTED, //бронирование отклонено владельцем
    CANCELED //бронирование отменено создателем
}