package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.comments.model.dto.CommentResponse;
import ru.practicum.shareit.item.dto.ItemBookingData;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ResponseWithBookingData;
import ru.practicum.shareit.item.dto.ResponseWithComment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public static ResponseWithBookingData toItemBookingData(Item item, ItemBookingData itemBrookingData) {
        return new ResponseWithBookingData(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null,
                itemBrookingData.getNext(),
                itemBrookingData.getLast());
    }

    public static ResponseWithComment toResponseWithComment(Item item,
                                                            ItemBookingData itemBrookingData,
                                                            List<CommentResponse> comments) {
        return new ResponseWithComment(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getRequest() != null ? item.getRequest().getId() : null,
                itemBrookingData.getNext(),
                itemBrookingData.getLast(),
                comments);
    }
}