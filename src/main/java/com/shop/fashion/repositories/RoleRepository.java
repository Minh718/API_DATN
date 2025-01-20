package com.shop.fashion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<com.shop.fashion.entities.Role, String> {
}