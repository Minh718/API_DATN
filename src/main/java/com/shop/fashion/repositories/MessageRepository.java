package com.shop.fashion.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.idChatBox = :chatBoxId ORDER BY m.createdAt DESC")
    List<Message> findTopMessages(Long chatBoxId, Pageable pageable);
}
