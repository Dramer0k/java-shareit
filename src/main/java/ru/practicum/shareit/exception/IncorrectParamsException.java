package ru.practicum.shareit.exception;

public class IncorrectParamsException extends RuntimeException {
    public IncorrectParamsException(String message) {
        super(message);
    }
}