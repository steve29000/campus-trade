package com.campustrade.common.exception;

import com.campustrade.common.response.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void businessExceptionUsesItsOwnCodeAndMessage() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.CONFLICT.getCode()))
                .andExpect(jsonPath("$.message").value("username already exists"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void unexpectedExceptionFallsBackToSystemError() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SYSTEM_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("internal server error"));
    }

    @Test
    void malformedJsonBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("request body is invalid"));
    }

    @Test
    void pathVariableTypeMismatchReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/number/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("parameter 'id' is invalid"));
    }

    @RestController
    private static class ThrowingController {

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(ResultCode.CONFLICT, "username already exists");
        }

        @GetMapping("/test/boom")
        void boom() {
            throw new IllegalStateException("something broke");
        }

        @PostMapping("/test/body")
        void body(@RequestBody Payload payload) {
            // 仅用于触发请求体解析，不会真正执行
        }

        @GetMapping("/test/number/{id}")
        void number(@PathVariable("id") Long id) {
            // 仅用于触发路径变量类型转换
        }

        private record Payload(String name) {
        }
    }
}
