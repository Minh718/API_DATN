package com.shop.fashion.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.User;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.role.name = :role")
    Optional<User> findByEmailAndRole(String email, String role);

    @Query("SELECT u FROM User u WHERE u.email = :username OR u.phone = :username")
    Optional<User> findByEmailOrPhone(String username);

    @Query("SELECT u FROM User u WHERE u.idUserGoogle = :idUserGoogle")
    Optional<User> findByidUserGoogle(String idUserGoogle);

    Optional<User> findByPhone(String phone);

    Optional<User> findByRolesName(String role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cart WHERE u.id = :userId")
    Optional<User> findByIdWithCart(@Param("userId") String userId);
}