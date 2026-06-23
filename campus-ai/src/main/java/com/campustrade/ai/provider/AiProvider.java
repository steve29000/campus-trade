package com.campustrade.ai.provider;

import com.campustrade.ai.dto.CategoryPredictRequest;
import com.campustrade.ai.dto.CategoryPredictResponse;
import com.campustrade.ai.dto.ContentCheckRequest;
import com.campustrade.ai.dto.ContentCheckResponse;
import com.campustrade.ai.dto.DescriptionOptimizeRequest;
import com.campustrade.ai.dto.DescriptionOptimizeResponse;
import com.campustrade.ai.dto.PriceSuggestRequest;
import com.campustrade.ai.dto.PriceSuggestResponse;

public interface AiProvider {

    DescriptionOptimizeResponse optimizeDescription(DescriptionOptimizeRequest request);

    CategoryPredictResponse predictCategory(CategoryPredictRequest request);

    ContentCheckResponse checkContent(ContentCheckRequest request);

    PriceSuggestResponse suggestPrice(PriceSuggestRequest request);
}
