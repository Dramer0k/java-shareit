package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.dto.ItemInRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.request.model.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private ItemRepository itemRepository;
    private ItemRequestRepository itemRequestRepository;
    private UserRepository userRepository;

    @Override
    public ItemRequestResponse addRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = itemRequestRepository.save(ItemRequestMapper.toItemRequest(itemRequestDto, user));
        return ItemRequestMapper.toResponse(request);
    }

    @Override
    public List<ItemRequestWithAnswer> getRequestsByRequestor(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> allItems = itemRepository.findAllByRequestIdIn(requestIds);

        List<ItemInRequest> itemInRequestList = allItems.stream().map(ItemRequestMapper::toItemInRequest).toList();

        return requests.stream()
                .map(itemRequest ->
                        ItemRequestMapper.toRequestWithAnswer(itemRequest, itemInRequestList))
                .toList();
    }

    @Override
    public List<ItemRequestResponse> getAllRequest(Long userId) {
        log.info("User_Id: {}", userId);

        List<ItemRequest> requestList = itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId);

        log.info("Request_list: {}", requestList);

        return requestList.stream()
                .map(ItemRequestMapper::toResponse)
                .toList();
    }

    @Override
    public ItemRequestWithAnswer findById(Long requestId) {
        ItemRequest itemInRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден!"));

        List<Item> itemList = itemRepository.findAllByRequestId(itemInRequest.getId());
        List<ItemInRequest> itemInRequestList = itemList.stream().map(ItemRequestMapper::toItemInRequest).toList();

        return ItemRequestMapper.toRequestWithAnswer(itemInRequest, itemInRequestList);
    }
}