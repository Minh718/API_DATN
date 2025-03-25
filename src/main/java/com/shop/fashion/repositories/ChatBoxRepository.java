package com.shop.fashion.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.ChatBox;

@Repository
public interface ChatBoxRepository extends JpaRepository<ChatBox, Long> {

    Optional<ChatBox> findByUserId(String userId);

    // Optional<InfoChatBox> findInfoChatBoxByUserId(String userId);
}
