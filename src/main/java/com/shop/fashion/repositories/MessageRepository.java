package com.shop.fashion.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.fashion.dtos.dtosRes.projections.ListChatBox;
import com.shop.fashion.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.idChatBox = :chatBoxId ORDER BY m.createdAt DESC")
    List<Message> findTopMessages(Long chatBoxId, Pageable pageable);

    @Query(nativeQuery = true, value = "select cb.id as id, u.name as name, u.picture as image,cb.user_id as userId, cb.admin_readed as isSeen from chat_box cb join user u on cb.user_id = u.id\n"
            + //
            "where cb.admin_readed = 0 order by cb.updated_at desc;")
    List<ListChatBox> findChatBoxListUnSeenByAdmin();

    @Query(nativeQuery = true, value = "select cb.id as id, u.name as name, u.picture as image,cb.user_id as userId, cb.admin_readed as isSeen from chat_box cb join user u on cb.user_id = u.id\n"
            + //
            "order by cb.updated_at desc;")
    List<ListChatBox> findAllChatBoxListForAdmin();
}
