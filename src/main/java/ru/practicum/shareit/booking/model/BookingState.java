package ru.practicum.shareit.booking.model;

public enum BookingState {
    ALL,
    CURRENT, //текущие
    PAST, //завершённые
    FUTURE, //будущие
    WAITING, //ожидающие подтверждения
    REJECTED //отклонённые
}