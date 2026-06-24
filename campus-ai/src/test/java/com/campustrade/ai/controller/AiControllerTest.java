package com.campustrade.ai.controller;

import com.campustrade.ai.dto.ContentCheckResponse;
import com.campustrade.ai.dto.DescriptionOptimizeResponse;
import com.campustrade.ai.provider.MockAiProvider;
import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiControllerTest {

    private final AiController aiController = new AiController(new MockAiProvider());

    @Test
    void optimizeDescriptionRejectsBlankRequestBodyFields() {
        ApiResponse<DescriptionOptimizeResponse> response = aiController.optimizeDescription(null);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("title and description are required");
        assertThat(response.data()).isNull();
    }

    @Test
    void checkContentRejectsBlankContent() {
        ApiResponse<ContentCheckResponse> response = aiController.checkContent(null);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("content is required");
        assertThat(response.data()).isNull();
    }
}
