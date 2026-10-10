package com.travel.controller;

import com.travel.common.Result;
import com.travel.service.AiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 旅游问答控制器
 *
 * 功能：在攻略详情页内置 AI 问答，自动携带当前景点上下文
 * 接口：POST /ai/ask
 * 请求体：{ "strategyId": 1, "question": "这里怎么去？" }
 *
 * 知识点：
 * - 场景化问答：后端根据 strategyId 获取景点信息，作为上下文注入回答
 * - 专一性保证：AiQuestionService 内部对非旅游问题进行拦截
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiQuestionController {

    private final AiQuestionService aiQuestionService;

    /**
     * AI 问答接口
     * @param body 请求体，包含 strategyId 和 question
     * @return AI 回答内容
     */
    @PostMapping("/ask")
    public Result<String> ask(@RequestBody Map<String, Object> body) {
        Long strategyId = Long.valueOf(body.get("strategyId").toString());
        String question = (String) body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Result.fail(400, "问题不能为空");
        }
        String answer = aiQuestionService.ask(strategyId, question.trim());
        return Result.success(answer);
    }
}
