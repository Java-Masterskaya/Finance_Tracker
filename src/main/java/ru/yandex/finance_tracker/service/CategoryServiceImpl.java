package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.dto.input.CategoryCreateRequest;
import ru.yandex.finance_tracker.dto.output.CategoryInfoDto;
import ru.yandex.finance_tracker.exception.BadRequestException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.mapper.CategoryMapper;
import ru.yandex.finance_tracker.model.Category;
import ru.yandex.finance_tracker.storage.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryInfoDto> getAllCategories() {
        log.info("Получение всех активных категорий");
        List<Category> categories = categoryRepository.findAllByIsDeletedFalse();
        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Transactional
    public CategoryInfoDto createCategory(CategoryCreateRequest request) {
        log.info("Создание новой категории: {}", request.name());

        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
            log.error("Категория с названием {} уже существует", request.name());
            throw new BadRequestException("Category with name '%s' already exists".formatted(request.name()));
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDeleted(false);

        Category savedCategory = categoryRepository.save(category);
        log.info("Категория успешно создана с ID: {}", savedCategory.getId());

        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Удаление категории с ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with ID %d not found".formatted(categoryId)));

        if (category.isDeleted()) {
            throw new BadRequestException("Category is already deleted");
        }

        category.setDeleted(true);
        categoryRepository.save(category);

        log.info("Категория с ID: {} удалена", categoryId);
    }
}