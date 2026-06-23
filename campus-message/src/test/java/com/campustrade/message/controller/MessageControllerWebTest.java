package com.campustrade.message.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.message.dto.MessageCreateRequest;
import com.campustrade.message.dto.MessageResponse;
import com.campustrade.message.enums.MessageStatus;
import com.campustrade.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessageControllerWebTest {

    private final CapturingMessageService messageService = new CapturingMessageService();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new MessageController(messageService))
            .build();

    @Test
    void postRouteBindsJsonRequestAndReturnsMessageJson() throws Exception {
        mockMvc.perform(post("/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", 2L)
                        .content("""
                                {
                                  "productId": 100,
                                  "senderId": 99,
                                  "content": "在吗"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productId").value(100))
                .andExpect(jsonPath("$.data.senderId").value(2))
                .andExpect(jsonPath("$.data.status").value("VISIBLE"));

        assertThat(messageService.createRequest.productId()).isEqualTo(100L);
        assertThat(messageService.createRequest.senderId()).isEqualTo(99L);
        assertThat(messageService.createRequest.content()).isEqualTo("在吗");
        assertThat(messageService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void listRouteBindsPathVariableAndSerializesList() throws Exception {
        mockMvc.perform(get("/message/product/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].productId").value(100));

        assertThat(messageService.productId).isEqualTo(100L);
    }

    @Test
    void hideRouteBindsPathVariableAndReturnsHiddenStatus() throws Exception {
        mockMvc.perform(put("/message/7/hide")
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));

        assertThat(messageService.id).isEqualTo(7L);
        assertThat(messageService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void deleteRouteBindsPathVariable() throws Exception {
        mockMvc.perform(delete("/message/7")
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertThat(messageService.id).isEqualTo(7L);
        assertThat(messageService.authenticatedUserId).isEqualTo(2L);
    }

    private static class CapturingMessageService extends MessageService {

        private MessageCreateRequest createRequest;
        private Long productId;
        private Long id;
        private Long authenticatedUserId;

        private CapturingMessageService() {
            super(null, null, null);
        }

        @Override
        public ApiResponse<MessageResponse> post(MessageCreateRequest request, Long authenticatedUserId) {
            this.createRequest = request;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(new MessageResponse(7L, request.productId(), authenticatedUserId,
                    request.content(), MessageStatus.VISIBLE));
        }

        @Override
        public ApiResponse<List<MessageResponse>> listByProductId(Long productId) {
            this.productId = productId;
            return ApiResponse.success(List.of(
                    new MessageResponse(7L, productId, 2L, "在吗", MessageStatus.VISIBLE)));
        }

        @Override
        public ApiResponse<MessageResponse> hide(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(new MessageResponse(id, 100L, authenticatedUserId, "在吗", MessageStatus.HIDDEN));
        }

        @Override
        public ApiResponse<Void> delete(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success();
        }
    }
}
