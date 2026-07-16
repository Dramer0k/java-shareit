package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponse addRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestWithAnswer> getRequestsByRequestor(Long userId);

    List<ItemRequestResponse> getAllRequest(Long userId);

    ItemRequestWithAnswer findById(Long requestId);
}