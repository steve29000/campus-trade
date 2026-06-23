package com.campustrade.product.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Sentinel 限流规则（代码方式，便于演示和复现；后续可改为从 Nacos 数据源动态下发）。
 *
 * <p>对商品列表接口 {@code GET /product}（资源名即 URL 路径）限制 QPS，超过阈值的请求会被
 * {@link SentinelBlockHandler} 拦截并返回统一的「请求过于频繁」响应。</p>
 */
@Configuration
public class SentinelFlowConfig {

    @PostConstruct
    public void initFlowRules() {
        FlowRule rule = new FlowRule();
        rule.setResource("/product");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(3);
        FlowRuleManager.loadRules(List.of(rule));
    }
}
