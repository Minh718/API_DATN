package com.shop.fashion.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.ArgumentMatchers.*;

import com.shop.fashion.dtos.dtosReq.AdminSendMess;
import com.shop.fashion.dtos.dtosReq.UserSendMess;
import com.shop.fashion.dtos.dtosRes.MessageDTO;
import com.shop.fashion.entities.ChatBox;
import com.shop.fashion.entities.Message;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.repositories.ChatBoxRepository;
import com.shop.fashion.repositories.MessageRepository;

import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ChatBoxRepository chatBoxRepository;
    @Mock
    private MessageRepository messageRepository;
    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setupSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testUserSendMessage_success() {
        UserSendMess message = new UserSendMess();
        message.setMessage("Hello!");

        ChatBox chatBox = new ChatBox();
        chatBox.setId(1L);

        Message savedMessage = new Message();
        savedMessage.setIdChatBox(1L);
        savedMessage.setIdSend("user123");
        savedMessage.setMessage("Hello!");

        when(chatBoxRepository.findByUserId("user123")).thenReturn(Optional.of(chatBox));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        MessageDTO result = messageService.userSendMessage(message);

        assertEquals("Hello!", result.getMessage());
        verify(chatBoxRepository).save(any(ChatBox.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testUserSendMessage_chatBoxNotFound() {
        when(chatBoxRepository.findByUserId("user123")).thenReturn(Optional.empty());

        UserSendMess message = new UserSendMess();
        message.setMessage("Hello!");

        assertThrows(CustomException.class, () -> messageService.userSendMessage(message));
    }

    @Test
    void testUserGetMessages_success() {
        ChatBox chatBox = new ChatBox();
        chatBox.setId(1L);

        Message msg = new Message();
        msg.setMessage("Hi there");

        when(chatBoxRepository.findByUserId("user123")).thenReturn(Optional.of(chatBox));
        when(messageRepository.findTopMessages(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(msg));

        List<MessageDTO> result = messageService.userGetMessages(5);

        assertEquals(1, result.size());
        assertEquals("Hi there", result.get(0).getMessage());
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    void testAdminGetMessages_success() {
        ChatBox chatBox = new ChatBox();
        chatBox.setId(99L);

        Message msg = new Message();
        msg.setMessage("Admin message");

        when(chatBoxRepository.findById(99L)).thenReturn(Optional.of(chatBox));
        when(messageRepository.findTopMessages(eq(99L), any(Pageable.class)))
                .thenReturn(List.of(msg));

        List<MessageDTO> result = messageService.adminGetMessages(99L, 10);

        assertEquals(1, result.size());
        assertEquals("Admin message", result.get(0).getMessage());
    }

    @Test
    void testAdminSendMessage_success() {
        AdminSendMess adminSendMess = new AdminSendMess();
        adminSendMess.setMessage("From admin");
        adminSendMess.setChatBoxId(10L);

        ChatBox chatBox = new ChatBox();
        chatBox.setId(10L);

        Message msg = new Message();
        msg.setIdChatBox(10L);
        msg.setMessage("From admin");

        when(chatBoxRepository.findById(10L)).thenReturn(Optional.of(chatBox));
        when(messageRepository.save(any(Message.class))).thenReturn(msg);

        MessageDTO result = messageService.adminSendMessage(adminSendMess);

        assertEquals("From admin", result.getMessage());
        verify(chatBoxRepository).save(any(ChatBox.class));
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testAdminSendMessage_chatBoxNotFound() {
        AdminSendMess adminSendMess = new AdminSendMess();
        adminSendMess.setChatBoxId(999L);

        when(chatBoxRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> messageService.adminSendMessage(adminSendMess));
    }
}
