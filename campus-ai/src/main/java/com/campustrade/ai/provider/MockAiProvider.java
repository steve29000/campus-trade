package com.campustrade.ai.provider;

import com.campustrade.ai.dto.CategoryPredictRequest;
import com.campustrade.ai.dto.CategoryPredictResponse;
import com.campustrade.ai.dto.ContentCheckRequest;
import com.campustrade.ai.dto.ContentCheckResponse;
import com.campustrade.ai.dto.DescriptionOptimizeRequest;
import com.campustrade.ai.dto.DescriptionOptimizeResponse;
import com.campustrade.ai.dto.PriceSuggestRequest;
import com.campustrade.ai.dto.PriceSuggestResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
public class MockAiProvider implements AiProvider {

    private static final List<String> PROHIBITED_KEYWORDS = List.of(
            "枪",
            "毒品",
            "管制刀具",
            "假证",
            "赌博"
    );

    @Override
    public DescriptionOptimizeResponse optimizeDescription(DescriptionOptimizeRequest request) {
        String title = normalize(request == null ? null : request.title(), "闲置好物");
        String description = normalize(request == null ? null : request.description(), "商品状态良好，适合同校同学面交");

        String optimizedDescription = "【" + title + "】" + description
                + "。物品保存较好，适合校园学习和日常使用，支持当面查看，欢迎同校同学咨询。";

        return new DescriptionOptimizeResponse(optimizedDescription);
    }

    @Override
    public CategoryPredictResponse predictCategory(CategoryPredictRequest request) {
        String text = joinText(
                request == null ? null : request.title(),
                request == null ? null : request.description()
        );

        if (containsAny(text, "手机", "电脑", "平板", "ipad", "iphone", "耳机", "相机", "键盘")) {
            return new CategoryPredictResponse("数码", 0.90);
        }
        if (containsAny(text, "台灯", "收纳", "杯", "床垫", "被子", "衣架", "生活")) {
            return new CategoryPredictResponse("生活用品", 0.82);
        }
        if (containsAny(text, "书桌", "书架")) {
            return new CategoryPredictResponse("生活用品", 0.82);
        }
        if (containsAny(text, "教材", "真题", "考研", "四六级", "小说", "资料")
                || containsStandaloneBookKeyword(text)) {
            return new CategoryPredictResponse("图书", 0.86);
        }
        if (containsAny(text, "球", "篮球", "足球", "羽毛球", "跑步", "瑜伽", "运动", "户外")) {
            return new CategoryPredictResponse("运动户外", 0.84);
        }

        return new CategoryPredictResponse("其他", 0.50);
    }

    @Override
    public ContentCheckResponse checkContent(ContentCheckRequest request) {
        String content = joinText(request == null ? null : request.content());
        for (String keyword : PROHIBITED_KEYWORDS) {
            if (content.contains(keyword.toLowerCase(Locale.ROOT))) {
                return new ContentCheckResponse(false, "content contains prohibited keyword: " + keyword);
            }
        }

        return new ContentCheckResponse(true, "content passed mock safety check");
    }

    @Override
    public PriceSuggestResponse suggestPrice(PriceSuggestRequest request) {
        String category = normalize(request == null ? null : request.category(), "其他");
        String condition = joinText(
                request == null ? null : request.title(),
                request == null ? null : request.description()
        );

        int[] range = baseRange(category);
        double factor = conditionFactor(condition);
        int min = range[0];
        int max = range[1];
        long suggested = Math.round(min + (max - min) * factor);

        String reason = "基于分类「" + category + "」" + conditionHint(factor)
                + "给出参考价，建议区间 " + min + "-" + max + " 元，可按成色和配件适当浮动。";

        return new PriceSuggestResponse(
                BigDecimal.valueOf(suggested),
                BigDecimal.valueOf(min),
                BigDecimal.valueOf(max),
                reason
        );
    }

    private int[] baseRange(String category) {
        String c = category.toLowerCase(Locale.ROOT);
        if (c.contains("数码")) {
            return new int[]{200, 3000};
        }
        if (c.contains("图书") || c.contains("书")) {
            return new int[]{10, 80};
        }
        if (c.contains("生活")) {
            return new int[]{15, 200};
        }
        if (c.contains("运动") || c.contains("户外")) {
            return new int[]{30, 600};
        }
        return new int[]{20, 300};
    }

    private double conditionFactor(String text) {
        if (containsAny(text, "全新", "未拆封", "未使用", "99新", "几乎全新")) {
            return 0.8;
        }
        if (containsAny(text, "旧", "划痕", "磨损", "破损", "瑕疵", "屏碎")) {
            return 0.25;
        }
        return 0.5;
    }

    private String conditionHint(double factor) {
        if (factor >= 0.8) {
            return "且描述接近全新，";
        }
        if (factor <= 0.25) {
            return "且描述有使用痕迹，";
        }
        return "按正常二手成色，";
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                builder.append(value.trim().toLowerCase(Locale.ROOT)).append(' ');
            }
        }
        return builder.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsStandaloneBookKeyword(String text) {
        return text.contains("书 ")
                || text.contains(" 书")
                || text.equals("书");
    }
}
