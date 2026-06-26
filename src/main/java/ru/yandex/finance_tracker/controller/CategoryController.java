package ru.yandex.finance_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.input.CategoryCreateRequest;
import ru.yandex.finance_tracker.dto.output.CategoryInfoDto;
import ru.yandex.finance_tracker.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryInfoDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryInfoDto createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return categoryService.createCategory(request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(@PathVariable(name = "categoryId") @Positive Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}
