package com.travel.service;

import com.travel.domain.entity.Strategy;
import com.travel.service.impl.StrategyServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * AI 旅游问答服务
 *
 * 设计思路：
 * 1. 场景化：自动携带当前攻略的城市、景点、攻略内容作为上下文
 * 2. 专一性：对非旅游类问题进行友好拦截
 * 3. 可扩展：当前基于规则匹配+上下文模板生成回答，预留了接入真实大模型的位置
 *
 * 知识点：
 * - 上下文注入：把景点信息拼进 prompt，让回答更精准
 * - 意图识别：通过关键词判断是否为旅游相关问题
 * - 模板化回答：针对常见旅游问题（交通、美食、住宿、最佳季节等）生成结构化回答
 */
@Service
@RequiredArgsConstructor
public class AiQuestionService {

    private final StrategyService strategyService;

    /** 旅游相关关键词（用于判断问题是否属于旅游场景） */
    private static final List<String> TRAVEL_KEYWORDS = Arrays.asList(
            "旅游", "旅行", "攻略", "景点", "门票", "开放", "时间", "交通", "怎么去", "地铁", "公交",
            "美食", "吃", "餐厅", "小吃", "住宿", "酒店", "民宿", "住", "天气", "穿", "季节", "最佳",
            "推荐", "好玩", "值得", "路线", "行程", "几天", "多少钱", "价格", "收费", "免费",
            "拍照", "打卡", "网红", "文化", "历史", "建筑", "风景", "自然", "爬山", "徒步",
            "注意", "事项", "准备", "带什么", "禁忌", "风俗", "特产", "纪念品", "周边", "附近",
            " city", "travel", "food", "hotel", "weather", "ticket"
    );

    /** 非旅游拦截关键词（明确不属于旅游场景的问题） */
    private static final List<String> BLOCK_KEYWORDS = Arrays.asList(
            "代码", "编程", "java", "python", "bug", "报错", "编译", "函数", "变量",
            "股票", "基金", "理财", "炒股", "k线",
            "医疗", "看病", "吃药", "医院", "手术", "病症",
            "政治", "选举", "总统", "主席"
    );

    /**
     * AI 问答核心方法
     * @param strategyId 攻略ID（用于获取景点上下文）
     * @param question 用户问题
     * @return AI 回答
     */
    public String ask(Long strategyId, String question) {
        // 1. 非旅游问题拦截
        if (!isTravelQuestion(question)) {
            return "抱歉，我是旅游小助手，只能回答旅游相关的问题哦~ 🗺️\n" +
                   "比如景点介绍、交通路线、美食推荐、住宿建议、最佳游玩季节等。";
        }

        // 2. 获取攻略上下文（城市、景点、标签、分类、内容摘要）
        Strategy strategy = strategyService.getById(strategyId);
        String city = strategy != null ? strategy.getCityName() : "未知";
        String spot = strategy != null ? strategy.getSpotName() : "未知";
        String category = strategy != null ? strategy.getCategory() : "";
        String tag = strategy != null ? strategy.getTag() : "";
        String content = strategy != null && strategy.getContent() != null
                ? strategy.getContent().substring(0, Math.min(200, strategy.getContent().length()))
                : "";

        // 3. 根据问题类型生成回答
        return generateAnswer(city, spot, category, tag, content, question);
    }

    /**
     * 判断是否为旅游相关问题
     * 规则：包含旅游关键词 或 不包含拦截关键词
     */
    private boolean isTravelQuestion(String question) {
        if (question == null || question.trim().isEmpty()) return false;
        String q = question.toLowerCase();
        // 命中拦截关键词 → 直接拦截
        for (String kw : BLOCK_KEYWORDS) {
            if (q.contains(kw.toLowerCase())) return false;
        }
        // 命中旅游关键词 → 放行
        for (String kw : TRAVEL_KEYWORDS) {
            if (q.contains(kw.toLowerCase())) return true;
        }
        // 都没命中：如果问题较短（可能是口语化提问），也放行，让模板兜底
        return q.length() <= 20;
    }

    /**
     * 根据问题类型和景点上下文生成回答
     */
    private String generateAnswer(String city, String spot, String category, String tag, String content, String question) {
        String q = question.toLowerCase();
        StringBuilder sb = new StringBuilder();

        // 通用开场白，带上景点上下文
        sb.append("关于【").append(spot).append("】（").append(city).append("）的问题，我来为你解答：\n\n");

        // 交通类问题
        if (q.contains("交通") || q.contains("怎么去") || q.contains("地铁") || q.contains("公交") || q.contains("路线")) {
            sb.append("🚇 交通指南：\n");
            sb.append("• 可以乘坐高铁/飞机到达").append(city).append("，再换乘市内交通前往").append(spot).append("。\n");
            sb.append("• 市内可乘坐地铁或公交，建议使用地图APP导航，选择公共交通出行更环保。\n");
            sb.append("• 如果距离较远，也可以打车或网约车，注意避开早晚高峰。\n");
            return sb.toString();
        }

        // 美食类问题
        if (q.contains("美食") || q.contains("吃") || q.contains("餐厅") || q.contains("小吃")) {
            sb.append("🍜 美食推荐：\n");
            sb.append("• ").append(city).append("的特色美食不容错过，建议尝试当地老字号餐厅。\n");
            sb.append("• 可以在点评APP上搜索").append(city).append("美食排行榜，选择评分高的店铺。\n");
            sb.append("• 注意：景区内餐饮价格偏高，可选择景区外用餐，性价比更高。\n");
            return sb.toString();
        }

        // 住宿类问题
        if (q.contains("住宿") || q.contains("酒店") || q.contains("民宿") || q.contains("住")) {
            sb.append("🏨 住宿建议：\n");
            sb.append("• 建议选择").append(spot).append("周边或市中心的酒店，出行更方便。\n");
            sb.append("• 预算充足可选星级酒店，追求性价比可选连锁酒店或民宿。\n");
            sb.append("• 旺季建议提前1-2周预订，避免满房。\n");
            return sb.toString();
        }

        // 门票/收费类问题
        if (q.contains("门票") || q.contains("价格") || q.contains("收费") || q.contains("免费") || q.contains("多少钱")) {
            sb.append("🎫 门票信息：\n");
            sb.append("• 该景点标签为「").append(tag != null ? tag : "未知").append("」，");
            if ("免费".equals(tag)) {
                sb.append("免费开放，无需购票。\n");
            } else {
                sb.append("具体门票价格请以景区官网或售票窗口为准。\n");
            }
            sb.append("• 建议提前在官方渠道购票，避免现场排队。\n");
            sb.append("• 部分景区对学生、老人有优惠，记得带好证件。\n");
            return sb.toString();
        }

        // 开放时间类问题
        if (q.contains("开放") || q.contains("时间") || q.contains("几点") || q.contains("关门")) {
            sb.append("🕐 开放时间：\n");
            sb.append("• 一般景区开放时间为 08:00-18:00，具体以景区公告为准。\n");
            sb.append("• 建议上午前往，人流量较少，体验更好。\n");
            sb.append("• 注意：部分景区周一闭馆维护，出行前请确认。\n");
            return sb.toString();
        }

        // 天气/穿着类问题
        if (q.contains("天气") || q.contains("穿") || q.contains("季节") || q.contains("最佳")) {
            sb.append("🌤 天气与穿着：\n");
            sb.append("• 出行前请查看").append(city).append("的天气预报，根据天气选择衣物。\n");
            sb.append("• 春秋季节温差大，建议携带薄外套；夏季注意防晒；冬季注意保暖。\n");
            sb.append("• 最佳游玩季节因景点而异，").append(category != null ? category : "").append("类景点四季皆宜。\n");
            return sb.toString();
        }

        // 游玩攻略/推荐类问题
        if (q.contains("好玩") || q.contains("推荐") || q.contains("值得") || q.contains("攻略") || q.contains("行程") || q.contains("几天")) {
            sb.append("📍 游玩攻略：\n");
            sb.append("• 这篇攻略的作者分享了在").append(spot).append("的游玩体验，").append(city).append("还有很多值得探索的地方。\n");
            if (content != null && !content.isEmpty()) {
                sb.append("• 攻略要点：").append(content).append("\n");
            }
            sb.append("• 建议安排 ").append("1-2天 ").append("深度游玩，慢慢感受当地的文化和风景。\n");
            sb.append("• 记得拍照打卡，留下美好的旅行回忆！📷\n");
            return sb.toString();
        }

        // 注意事项/准备类问题
        if (q.contains("注意") || q.contains("准备") || q.contains("带什么") || q.contains("禁忌") || q.contains("风俗")) {
            sb.append("📋 注意事项：\n");
            sb.append("• 出行前准备好身份证、手机、充电宝等必备物品。\n");
            sb.append("• 尊重当地风俗习惯，文明旅游。\n");
            sb.append("• 注意人身和财物安全，避免在偏僻角落逗留。\n");
            sb.append("• 保护环境，不乱扔垃圾，做文明游客。🌱\n");
            return sb.toString();
        }

        // 通用兜底回答
        sb.append("关于「").append(question).append("」：\n");
        sb.append("• ").append(spot).append("位于").append(city).append("，");
        if (category != null && !category.isEmpty()) {
            sb.append("属于").append(category).append("类景点，");
        }
        sb.append("是一个值得一游的地方。\n");
        if (content != null && !content.isEmpty()) {
            sb.append("• 攻略摘要：").append(content).append("\n");
        }
        sb.append("• 如果你有更具体的问题（如交通、美食、住宿等），欢迎继续提问！\n");
        return sb.toString();
    }
}
