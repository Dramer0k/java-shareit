package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i " +
            "from Item as i " +
            "JOIN FETCH i.owner " +
            "where (LOWER(i.name) LIKE CONCAT('%', LOWER(?1), '%') " +
            "OR LOWER(i.description) LIKE CONCAT('%', LOWER(?1), '%')) " +
            "AND i.available IS TRUE"
    )
    List<Item> search(String text);

    List<Item> findByIdIn(List<Long> itemIds);

    List<Item> findAllByRequestId(Long id);
}