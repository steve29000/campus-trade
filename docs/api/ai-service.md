# campus-ai API

This document describes the current `campus-ai` mock API. It is intended for local course-project demos and early integration notes before Swagger/Knife4j is added.

## Current Phase Notes

- `campus-ai` currently exposes deterministic mock endpoints for description optimization, category prediction, and content checking.
- `MockAiProvider` is mock-only. It uses local keyword and template rules, returns the same output for the same input, and does not call external AI models, SDKs, networks, or persisted services.
- Responses use the shared `ApiResponse` envelope:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

The `code` field is an application-level response body code. The current skeleton does not map these codes to HTTP status codes with `ResponseEntity`, `@ResponseStatus`, or global exception handling yet.

## Optimize Description

`POST /ai/description/optimize`

Builds a more polished campus-trade listing description from a title and a short seller description. `title` and `description` are required.

### Request

```json
{
  "title": "iPad Air",
  "description": "自用一年，功能正常"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "optimizedDescription": "【iPad Air】自用一年，功能正常。物品保存较好，适合校园学习和日常使用，支持当面查看，欢迎同校同学咨询。"
  }
}
```

### Example Validation Failure

Missing title or description:

```json
{
  "code": 400,
  "message": "title and description are required",
  "data": null
}
```

## Predict Category

`POST /ai/category/predict`

Predicts a product category from the listing title and description. `title` and `description` are required.

The current mock rule set can return categories such as `数码`, `生活用品`, `图书`, `运动户外`, or `其他`.

### Request

```json
{
  "title": "iPhone 15",
  "description": "国行手机，电池健康"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "category": "数码",
    "confidence": 0.9
  }
}
```

### Example Validation Failure

Missing title or description:

```json
{
  "code": 400,
  "message": "title and description are required",
  "data": null
}
```

## Check Content

`POST /ai/content/check`

Checks listing text against the current mock prohibited-keyword list. `content` is required.

The mock safety check currently looks for these prohibited keywords: `枪`, `毒品`, `管制刀具`, `假证`, and `赌博`.

### Request

```json
{
  "content": "出一本高数教材，笔记完整"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "passed": true,
    "reason": "content passed mock safety check"
  }
}
```

### Example Safety Failure

Content containing a prohibited keyword returns a successful API envelope with `passed: false`, because the request was valid and the mock safety result is carried in `data`.

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "passed": false,
    "reason": "content contains prohibited keyword: 管制刀具"
  }
}
```

### Example Validation Failure

Missing content:

```json
{
  "code": 400,
  "message": "content is required",
  "data": null
}
```

## Suggest Price

`POST /ai/price/suggest`

Returns a deterministic mock price suggestion for a second-hand item. The price range is derived
from the `category`; the suggested point inside the range is nudged up for brand-new wording
(e.g. `全新`、`未拆封`) and down for worn wording (e.g. `划痕`、`磨损`) found in `title` / `description`.

### Request

```json
{
  "category": "数码",
  "title": "iPad Air",
  "description": "自用一年，功能正常"
}
```

### Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "suggestedPrice": 1600,
    "minPrice": 200,
    "maxPrice": 3000,
    "reason": "基于分类「数码」按正常二手成色，给出参考价，建议区间 200-3000 元，可按成色和配件适当浮动。"
  }
}
```

### Example Validation Failure

Missing category:

```json
{
  "code": 400,
  "message": "category is required",
  "data": null
}
```
