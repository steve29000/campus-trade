package com.campustrade.ai.controller;

import com.campustrade.ai.dto.CategoryPredictRequest;
import com.campustrade.ai.dto.CategoryPredictResponse;
import com.campustrade.ai.dto.ContentCheckRequest;
import com.campustrade.ai.dto.ContentCheckResponse;
import com.campustrade.ai.dto.DescriptionOptimizeRequest;
import com.campustrade.ai.dto.DescriptionOptimizeResponse;
import com.campustrade.ai.provider.AiProvider;
import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiProvider aiProvider;

    public AiController(AiProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    @PostMapping("/description/optimize")
    public ApiResponse<DescriptionOptimizeResponse> optimizeDescription(
            @RequestBody DescriptionOptimizeRequest request
    ) {
        if (request == null || isBlank(request.title()) || isBlank(request.description())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "title and description are required");
        }
        return ApiResponse.success(aiProvider.optimizeDescription(request));
    }

    @PostMapping("/category/predict")
    public ApiResponse<CategoryPredictResponse> predictCategory(@RequestBody CategoryPredictRequest request) {
        if (request == null || isBlank(request.title()) || isBlank(request.description())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "title and description are required");
        }
        return ApiResponse.success(aiProvider.predictCategory(request));
    }

    @PostMapping("/content/check")
    public ApiResponse<ContentCheckResponse> checkContent(@RequestBody ContentCheckRequest request) {
        if (request == null || isBlank(request.content())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "content is required");
        }
        return ApiResponse.success(aiProvider.checkContent(request));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
