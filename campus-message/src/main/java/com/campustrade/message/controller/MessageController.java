package com.campustrade.message.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.message.dto.MessageCreateRequest;
import com.campustrade.message.dto.MessageResponse;
import com.campustrade.message.service.MessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ApiResponse<MessageResponse> post(@RequestBody MessageCreateRequest request) {
        return messageService.post(request);
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<MessageResponse>> listByProductId(@PathVariable("productId") Long productId) {
        return messageService.listByProductId(productId);
    }

    @PutMapping("/{id}/hide")
    public ApiResponse<MessageResponse> hide(@PathVariable("id") Long id) {
        return messageService.hide(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        return messageService.delete(id);
    }
}
