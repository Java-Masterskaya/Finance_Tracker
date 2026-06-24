package ru.yandex.finance_tracker.service;

import ru.yandex.finance_tracker.dto.input.CategoryCreateRequest;
import ru.yandex.finance_tracker.dto.output.CategoryInfoDto;

import java.util.List;

public interface CategoryService {
    List<CategoryInfoDto> getAllCategories();

    CategoryInfoDto createCategory(CategoryCreateRequest request);

    void deleteCategory(Long categoryId);
}