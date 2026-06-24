package ru.yandex.finance_tracker.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.dto.output.CategoryInfoDto;
import ru.yandex.finance_tracker.model.Category;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryInfoDto toDto(Category category) {
        if (category == null) return null;
        return new CategoryInfoDto(category.getId(), category.getName());
    }

    public List<CategoryInfoDto> toDtoList(List<Category> categories) {
        return categories.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}