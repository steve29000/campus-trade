package com.campustrade.ai.provider;

import com.campustrade.ai.dto.CategoryPredictRequest;
import com.campustrade.ai.dto.CategoryPredictResponse;
import com.campustrade.ai.dto.ContentCheckRequest;
import com.campustrade.ai.dto.ContentCheckResponse;
import com.campustrade.ai.dto.DescriptionOptimizeRequest;
import com.campustrade.ai.dto.DescriptionOptimizeResponse;
import com.campustrade.ai.dto.PriceSuggestRequest;
import com.campustrade.ai.dto.PriceSuggestResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiProviderTest {

    private final AiProvider aiProvider = new MockAiProvider();

    @Test
    void optimizeDescriptionBuildsPolishedDescriptionFromTitleAndDescription() {
        DescriptionOptimizeResponse response = aiProvider.optimizeDescription(
                new DescriptionOptimizeRequest("iPad Air", "自用一年，功能正常")
        );

        assertThat(response.optimizedDescription())
                .contains("iPad Air")
                .contains("自用一年，功能正常")
                .contains("适合校园学习和日常使用")
                .contains("欢迎同校同学咨询");
    }

    @Test
    void predictCategoryReturnsDigitalForDeviceKeywords() {
        CategoryPredictResponse response = aiProvider.predictCategory(
                new CategoryPredictRequest("iPhone 15", "国行手机，电池健康")
        );

        assertThat(response.category()).isEqualTo("数码");
        assertThat(response.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void predictCategoryReturnsBookForStudyKeywords() {
        CategoryPredictResponse response = aiProvider.predictCategory(
                new CategoryPredictRequest("考研英语书", "教材和真题")
        );

        assertThat(response.category()).isEqualTo("图书");
        assertThat(response.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void predictCategoryDoesNotTreatDeskAsBook() {
        CategoryPredictResponse response = aiProvider.predictCategory(
                new CategoryPredictRequest("宿舍书桌", "可折叠，适合学习")
        );

        assertThat(response.category()).isEqualTo("生活用品");
        assertThat(response.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void predictCategoryReturnsOtherWhenNoKeywordMatches() {
        CategoryPredictResponse response = aiProvider.predictCategory(
                new CategoryPredictRequest("毕业闲置", "有需要可以聊")
        );

        assertThat(response.category()).isEqualTo("其他");
        assertThat(response.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void checkContentPassesOrdinaryCampusItem() {
        ContentCheckResponse response = aiProvider.checkContent(
                new ContentCheckRequest("出一本高数教材，笔记完整")
        );

        assertThat(response.passed()).isTrue();
        assertThat(response.reason()).isEqualTo("content passed mock safety check");
    }

    @Test
    void checkContentRejectsProhibitedKeywords() {
        ContentCheckResponse response = aiProvider.checkContent(
                new ContentCheckRequest("出售管制刀具")
        );

        assertThat(response.passed()).isFalse();
        assertThat(response.reason()).contains("管制刀具");
    }

    @Test
    void suggestPriceReturnsDigitalRangeWithNormalCondition() {
        PriceSuggestResponse response = aiProvider.suggestPrice(
                new PriceSuggestRequest("数码", "iPad Air", "自用一年，功能正常")
        );

        assertThat(response.minPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.maxPrice()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(response.suggestedPrice()).isEqualByComparingTo(BigDecimal.valueOf(1600));
        assertThat(response.reason()).contains("数码");
    }

    @Test
    void suggestPriceLeansHighForBrandNewCondition() {
        PriceSuggestResponse response = aiProvider.suggestPrice(
                new PriceSuggestRequest("数码", "iPad Air", "全新未拆封，正品行货")
        );

        assertThat(response.suggestedPrice()).isEqualByComparingTo(BigDecimal.valueOf(2440));
    }

    @Test
    void suggestPriceLeansLowForWornCondition() {
        PriceSuggestResponse response = aiProvider.suggestPrice(
                new PriceSuggestRequest("数码", "iPad Air", "屏幕有划痕，使用痕迹明显")
        );

        assertThat(response.suggestedPrice()).isEqualByComparingTo(BigDecimal.valueOf(900));
    }

    @Test
    void suggestPriceReturnsBookRangeForBooks() {
        PriceSuggestResponse response = aiProvider.suggestPrice(
                new PriceSuggestRequest("图书", "考研英语", "教材和真题")
        );

        assertThat(response.minPrice()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(response.maxPrice()).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    void suggestPriceFallsBackToOtherRangeForUnknownCategory() {
        PriceSuggestResponse response = aiProvider.suggestPrice(
                new PriceSuggestRequest("盲盒", "毕业闲置", "有需要可以聊")
        );

        assertThat(response.minPrice()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(response.maxPrice()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }
}
