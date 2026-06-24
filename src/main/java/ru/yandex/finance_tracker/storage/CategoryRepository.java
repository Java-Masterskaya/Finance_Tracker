package ru.yandex.finance_tracker.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.finance_tracker.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByIsDeletedFalse();

    Optional<Category> findByIdAndIsDeletedFalse(Long id);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);
}