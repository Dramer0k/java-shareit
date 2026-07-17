package ru.practicum.shareit.exception;

public class NotEnoughPermitionException extends RuntimeException {
    public NotEnoughPermitionException(String message) {
        super(message);
    }
}