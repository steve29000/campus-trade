package com.campustrade.message.client.dto;

/**
 * 调用 campus-user 的最小响应 DTO，由留言服务自行维护，避免直接依赖用户模块的 DTO。
 */
public record UserProfileClientResponse(
        Long id,
        String username,
        String nickname
) {
}
