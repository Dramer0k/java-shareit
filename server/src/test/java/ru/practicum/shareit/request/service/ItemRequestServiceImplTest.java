package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponse;
import ru.practicum.shareit.request.model.dto.ItemRequestWithAnswer;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private static final Long USER_ID = 1L;
    private static final String DESCRIPTION = "Нужна вещь для поездки";

    @Test
    void addRequest_success() {
        User user = new User();
        user.setId(USER_ID);
        user.setName("Test User");
        user.setEmail("test@example.com");

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(DESCRIPTION);

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(1L);
        savedRequest.setDescription(DESCRIPTION);
        savedRequest.setRequestor(user);
        savedRequest.setCreated(LocalDateTime.now());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestResponse response = itemRequestService.addRequest(USER_ID, dto);

        verify(userRepository).findById(USER_ID);
        verify(itemRequestRepository).save(any(ItemRequest.class));

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(user, response.getRequestor());
    }

    @Test
    void addRequest_userNotFound_throwsNotFoundException() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(DESCRIPTION);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.addRequest(USER_ID, dto));
        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    void getRequestsByRequestor_success() {
        User user = new User();
        user.setId(USER_ID);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request 1");
        request1.setRequestor(user);
        request1.setCreated(LocalDateTime.now().minusDays(1));

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request 2");
        request2.setRequestor(user);
        request2.setCreated(LocalDateTime.now());

        List<ItemRequest> requests = List.of(request2, request1); // уже отсортировано по created desc

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(USER_ID)).thenReturn(requests);

        when(itemRepository.findAllByRequestIdIn(any())).thenReturn(List.of());

        List<ItemRequestWithAnswer> result = itemRequestService.getRequestsByRequestor(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(itemRequestRepository).findByRequestorIdOrderByCreatedDesc(USER_ID);
        verify(itemRepository, times(1)).findAllByRequestIdIn(any());

        assertEquals(2, result.size());
        assertEquals("Request 2", result.get(0).getDescription());
        assertEquals("Request 1", result.get(1).getDescription());
    }

    @Test
    void getRequestsByRequestor_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestsByRequestor(USER_ID));
        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(itemRequestRepository, itemRepository);
    }

    @Test
    void getAllRequest_success() {
        User otherUser = new User();
        otherUser.setId(2L);

        ItemRequest request = new ItemRequest();
        request.setId(3L);
        request.setDescription("Public request");
        request.setRequestor(otherUser);
        request.setCreated(LocalDateTime.now());

        List<ItemRequest> list = List.of(request);

        when(itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(USER_ID)).thenReturn(list);

        List<ItemRequestResponse> result = itemRequestService.getAllRequest(USER_ID);

        verify(itemRequestRepository).findByRequestor_IdNotOrderByCreatedDesc(USER_ID);
        verifyNoInteractions(userRepository, itemRepository); // userRepository не нужен в этом методе

        assertEquals(1, result.size());
        assertEquals(3L, result.getFirst().getId());
        assertEquals("Public request", result.getFirst().getDescription());
    }

    @Test
    void findById_success() {
        Long requestId = 1L;
        User requestor = new User();
        requestor.setId(USER_ID);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Single request");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(List.of());

        ItemRequestWithAnswer result = itemRequestService.findById(requestId);

        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).findAllByRequestId(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("Single request", result.getDescription());
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        Long requestId = 999L;
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.findById(requestId));
        verify(itemRequestRepository).findById(requestId);
        verifyNoInteractions(itemRepository);
    }
}