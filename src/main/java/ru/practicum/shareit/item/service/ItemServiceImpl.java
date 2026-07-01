package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IncorrectParamsException;
import ru.practicum.shareit.exception.NotEnoughPermitionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.model.dto.CommentRequest;
import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.comments.model.mapper.CommentMapper;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemBookingData;
import ru.practicum.shareit.item.dto.ItemBookingDataProjection;
import ru.practicum.shareit.item.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.dto.ResponseWithComment;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;

    @Override
    public Item addItem(Long userId, Item item) {
        log.info("Создаем предмет...");

        User owner = getUser(userId);
        item.setOwner(owner);

        if (item.getName() == null || item.getName().isBlank()) {
            throw new IncorrectParamsException("Name - обязательное поле");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IncorrectParamsException("Description - обязательное поле");
        }

        if ((item.getAvailable() == null)) {
            throw new IncorrectParamsException("Available - обязательное поле");
        }

        log.info("Item: {}", item);

        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) {
            User owner = getUser(userId);
            Item oldItem = getItem(itemId);

            if (!owner.getId().equals(oldItem.getOwner().getId())) {
                throw new NotEnoughPermitionException("Недостаточно прав для изменения!");
            }

            if (item.getName() != null) {
                oldItem.setName(item.getName());
            }

            if (item.getDescription() != null) {
                oldItem.setDescription(item.getDescription());
            }

            if (item.getAvailable() != oldItem.getAvailable()) {
                oldItem.setAvailable(item.getAvailable());
            }

            return itemRepository.save(oldItem);
        }

    @Override
    public ResponseWithComment getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет не найден"));

        ItemBookingDataProjection itemBookingDataProjection = bookingRepository.findBookingDateByItem(itemId);
        ItemBookingData itemBookingData = new ItemBookingData(
                itemBookingDataProjection.getId(),
                itemBookingDataProjection.getNextBooking(),
                itemBookingDataProjection.getLastBooking()
        );
        List<CommentResponse> comments = commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::toResponse)
                .toList();

        return ItemMapper.toResponseWithComment(item, itemBookingData, comments);
    }

    @Override
    public List<ResponseWithBookingData> getItems(Long userId) {

        List<ItemBookingData> bookingList = bookingRepository.findItemBookingDataByOwnerId(userId)
                .stream()
                .map(projection -> new ItemBookingData(
                        projection.getId(),
                        projection.getNextBooking(),
                        projection.getLastBooking()
                ))
                .toList();

        log.info("id: {}", bookingList.getFirst().getId());
        log.info("next: {}", bookingList.getFirst().getNext());
        log.info("last: {}", bookingList.getFirst().getLast());

        return bookingList.stream()
                .map(brookingData -> {
                    Item item = itemRepository.findById(brookingData.getId())
                            .orElseThrow(() -> new NotFoundException("Такой вещи не существует!"));
                    return ItemMapper.toItemBookingData(item, brookingData);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        log.info("Начался поиск по тексту: {}", text);
        return itemRepository.search(text);
    }

    @Override
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public CommentResponse setComment(Long userId, Long itemId, CommentRequest comment) {
        log.info("Создаем коммент...");
        Item item = getItem(itemId);
        log.info("Item: {}", item);

        List<Booking> bookings = bookingRepository
                .findAllByItemIdAndBookerIdEqualsAndEndIsBefore(
                        itemId,
                        userId,
                        LocalDateTime.now()
                );
        log.info("List<Booking> bookings: {}", bookings);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("User: {}", user);

        if (bookings.isEmpty()) {
            throw new IncorrectParamsException("Вы не арендовали эту вещь ранее или аренда еще не завершена");
        }

        log.info("Сохраняем коммент...");

        Comment result = commentRepository.save(CommentMapper.toComment(user, item, comment));

        log.info("result: {}", result);

        return CommentMapper.toResponse(result);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден!"));
    }
}