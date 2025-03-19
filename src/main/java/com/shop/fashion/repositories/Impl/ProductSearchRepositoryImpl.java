package com.shop.fashion.repositories.Impl;

import java.util.List;

import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.shop.fashion.entities.Product;
import com.shop.fashion.repositories.ProductSearchRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class ProductSearchRepositoryImpl implements ProductSearchRepository {
        @PersistenceContext
        private EntityManager entityManager;

        @Override
        public Page<Product> searchPublicProduct(String text, Pageable pageable, List<String> fields, String sortBy,
                        SortOrder sortOrder) {
                SearchSession searchSession = Search.session(entityManager);
                List<Product> result = searchSession
                                .search(Product.class)
                                .where(f -> f.bool()
                                                .must(f.match().fields(fields.toArray(String[]::new)).matching(text))
                                                .must(f.match().field("status").matching(true))) // Add condition for
                                                                                                 // status
                                // .sort(f -> f.field(sortBy).order(sortOrder))
                                .fetchHits((int) pageable.getOffset(), pageable.getPageSize());

                long totalHits = searchSession.search(Product.class)
                                .where(f -> f.bool()
                                                .must(f.match().fields("name").matching(text))
                                                .must(f.match().field("status").matching(true))) // Add condition for
                                                                                                 // status
                                .fetchTotalHitCount(); // Total count for pagination

                return new PageImpl<>(result, pageable, totalHits);
        }

}
