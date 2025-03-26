package com.shop.fashion.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.AdminSendMess;
import com.shop.fashion.dtos.dtosReq.UserSendMess;
import com.shop.fashion.dtos.dtosRes.InfoChatBox;
import com.shop.fashion.dtos.dtosRes.MessageDTO;
import com.shop.fashion.dtos.dtosRes.projections.ListChatBox;
import com.shop.fashion.entities.ChatBox;
import com.shop.fashion.entities.Message;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.MessageMapper;
import com.shop.fashion.repositories.ChatBoxRepository;
import com.shop.fashion.repositories.MessageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final ChatBoxRepository chatBoxRepository;
    private final UserService userService;
    private final MessageRepository messageRepository;

    public InfoChatBox getInfoChatBox() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatBox chatBox = chatBoxRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ChatBox not found"));
        String adminId = userService.getIdAdmin();
        return InfoChatBox.builder().idAdmin(adminId).idChatBox(chatBox.getId()).isSeen(chatBox.isUserReaded()).build();
    }

    public MessageDTO userSendMessage(UserSendMess message) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatBox chatBox = chatBoxRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_BOX_NOT_FOUND));
        chatBox.setAdminReaded(false);
        chatBox.setUpdatedAt(LocalDateTime.now());
        Message newMessage = new Message();
        newMessage.setIdChatBox(chatBox.getId());
        newMessage.setIdSend(userId);
        newMessage.setMessage(message.getMessage());
        messageRepository.save(newMessage);
        chatBoxRepository.save(chatBox);
        return MessageMapper.INSTANCE.toMessageDTO(newMessage);
    }

    @Transactional
    public List<MessageDTO> userGetMessages(int size) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatBox chatBox = chatBoxRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ChatBox not found"));
        chatBox.setUserReaded(true);
        Pageable pageable = PageRequest.of(0, size);
        List<Message> messages = messageRepository.findTopMessages(chatBox.getId(), pageable);
        return MessageMapper.INSTANCE.toMessageDTOs(messages);
    }

    @Transactional
    public List<MessageDTO> adminGetMessages(Long chatBoxId, int size) {
        ChatBox chatBox = chatBoxRepository.findById(chatBoxId)
                .orElseThrow(() -> new RuntimeException("ChatBox not found"));
        chatBox.setAdminReaded(true);
        Pageable pageable = PageRequest.of(0, size);
        List<Message> messages = messageRepository.findTopMessages(chatBox.getId(), pageable);
        return MessageMapper.INSTANCE.toMessageDTOs(messages);
    }

    public MessageDTO adminSendMessage(AdminSendMess adminSendMess) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        ChatBox chatBox = chatBoxRepository.findById(adminSendMess.getChatBoxId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_BOX_NOT_FOUND));
        chatBox.setUserReaded(false);
        chatBox.setUpdatedAt(LocalDateTime.now());
        Message newMessage = new Message();
        newMessage.setIdChatBox(chatBox.getId());
        newMessage.setIdSend(userId);
        newMessage.setMessage(adminSendMess.getMessage());
        messageRepository.save(newMessage);
        chatBoxRepository.save(chatBox);
        return MessageMapper.INSTANCE.toMessageDTO(newMessage);
    }

    public List<ListChatBox> getUnseenChatBoxForAdmin() {
        return messageRepository.findChatBoxListUnSeenByAdmin();
    };

    public List<ListChatBox> getAllChatBoxForAdmin() {
        return messageRepository.findAllChatBoxListForAdmin();
    };
}