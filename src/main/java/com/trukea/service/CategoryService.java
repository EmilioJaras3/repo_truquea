package com.trukea.service;

import com.trukea.repository.CategoryRepository;
import java.util.List;
import java.util.Map;

public class CategoryService {
    private CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public List<Map<String, Object>> getAllCategories() {
        return categoryRepository.findAll();
    }
}
