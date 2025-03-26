package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.AdminSendMess;
import com.shop.fashion.dtos.dtosReq.UserSendMess;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.InfoChatBox;
import com.shop.fashion.dtos.dtosRes.MessageDTO;
import com.shop.fashion.dtos.dtosRes.projections.ListChatBox;
import com.shop.fashion.services.MessageService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/infoChatBox")
    public ApiRes<InfoChatBox> getInfoChatBox() {
        return ApiRes.<InfoChatBox>builder().result(messageService.getInfoChatBox()).code(1000)
                .message("succesfully").build();
    }

    @PostMapping("/user/send")
    public ApiRes<MessageDTO> userSendMessage(@RequestBody UserSendMess message) {
        return ApiRes.<MessageDTO>builder().code(1000).result(messageService.userSendMessage(message))
                .message("succesfully").build();
    }

    @GetMapping("/user/get")
    public ApiRes<List<MessageDTO>> getMessages(@RequestParam(defaultValue = "10") int size) {
        return ApiRes.<List<MessageDTO>>builder().result(messageService.userGetMessages(size)).code(1000)
                .message("succesfully").build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/get")
    public ApiRes<List<MessageDTO>> getMessages(@RequestParam(defaultValue = "10") int size,
            @RequestParam Long chatBoxId) {
        return ApiRes.<List<MessageDTO>>builder().result(messageService.adminGetMessages(chatBoxId, size)).code(1000)
                .message("succesfully").build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/send")
    public ApiRes<MessageDTO> adminSendMessage(@RequestBody AdminSendMess adminSendMess) {
        return ApiRes.<MessageDTO>builder().code(1000).result(messageService.adminSendMessage(adminSendMess))
                .message("succesfully").build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/cbus/unseen")
    public ApiRes<List<ListChatBox>> getUnseenChatBoxForAdmin() {
        return ApiRes.<List<ListChatBox>>builder().code(1000).result(messageService.getUnseenChatBoxForAdmin())
                .message("succesfully").build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/cbs/all")
    public ApiRes<List<ListChatBox>> getAllChatBoxForAdmin() {
        return ApiRes.<List<ListChatBox>>builder().code(1000).result(messageService.getAllChatBoxForAdmin())
                .message("succesfully").build();
    }
}