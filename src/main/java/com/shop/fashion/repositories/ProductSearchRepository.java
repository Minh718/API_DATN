package com.shop.fashion.repositories;

import java.util.List;

import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shop.fashion.entities.Product;

public interface ProductSearchRepository {
    Page<Product> searchPublicProduct(String text, Pageable pageable, List<String> fields, String sortBy,
            SortOrder sortOrder);
}
