package com.shop.fashion.services;

import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.shop.fashion.entities.Product;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class IndexingService {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void indexExistingData() {
        MassIndexer indexer = Search.session(entityManager)
                .massIndexer(Product.class)
                .threadsToLoadObjects(5);
        try {
            indexer.startAndWait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Indexing interrupted", e);
        }
    }
}
