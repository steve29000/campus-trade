package com.campustrade.product.client;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.product.client.dto.ContentCheckClientRequest;
import com.campustrade.product.client.dto.ContentCheckClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 campus-ai 的内容安全检查能力。
 *
 * <p>商品发布前用它对标题和描述做违规检测，命中违规关键词时拒绝发布。</p>
 */
@FeignClient(name = "campus-ai")
public interface AiClient {

    @PostMapping("/ai/content/check")
    ApiResponse<ContentCheckClientResponse> checkContent(@RequestBody ContentCheckClientRequest request);
}
