package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.service.RagService;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("openAiChatClient")
    private ChatClient chatClient;

    @Autowired
    private RagService ragService;

    @PostMapping("/polish")
    public Result<String> polishDescription(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "");
        String description = request.getOrDefault("description", "");
        
        if (title.trim().isEmpty() && description.trim().isEmpty()) {
            return Result.error("标题和描述不能同时为空");
        }

        log.info("【AI 助手】收到文案润色请求，标题: [{}], 原描述: [{}]", title, description);

        try {
            String systemPrompt = "你是一个专业的二手电商平台闲置物品文案写手。请根据用户提供的简单商品信息，帮他生成一段排版精美、生动有吸引力、分段清晰的商品详情描述文案（包含商品亮点、转手原因、新旧程度说明等，如果用户提供的信息较少，你可以根据商品名称进行合理的脑补丰富）。\n" +
                                  "要求：\n" +
                                  "1. 严格控制字数在 500 字以内，文案要精炼。\n" +
                                  "2. 注意：你输出的内容将直接填充到用户的输入框中，因此**只需要返回文案本身，绝对不要包含任何客套话、开场白或结尾的提示语**。";
            
            String userPrompt = String.format("商品标题: %s\n原始描述: %s", title, description);

            List<org.springframework.ai.chat.messages.Message> messageList = new java.util.ArrayList<>();
            messageList.add(new SystemMessage(systemPrompt));
            messageList.add(new UserMessage(userPrompt));

            Prompt prompt = new Prompt(messageList);
            String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            
            log.info("【AI 助手】文案润色完成。");
            return Result.success(aiResponse.trim());
        } catch (Exception e) {
            log.error("【AI 助手】文案润色失败", e);
            return Result.error("文案润色失败，请稍后再试");
        }
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.warn("【AI 助手】收到空消息，已拒绝请求。");
            return Result.error("消息不能为空");
        }
        
        // 提取历史对话记录
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        log.info("【AI 助手】收到用户消息: [{}]", userMessage);
        log.info("【AI 助手】收到历史记录条数: [{}]", history != null ? history.size() : 0);

        try {
            log.info("【AI 助手】步骤 1: 开始在 PgVector 中进行 RAG 向量检索...");
            // 1. 先通过 RAG 从向量数据库检索可能相关的商品 (Top 3)，获取包含元数据的完整详情
            List<Map<String, Object>> relevantItemDetails = ragService.retrieveRelevantItemDetails(userMessage, 10);
            log.info("【AI 助手】步骤 1 完成: 共检索到 {} 条相关商品数据。", relevantItemDetails.size());
            
            // 2. 构建系统提示词，注入 RAG 上下文
            log.info("【AI 助手】步骤 2: 开始构建系统提示词...");
            StringBuilder systemPromptBuilder = new StringBuilder();
            systemPromptBuilder.append("你是 SwapU 校园闲置交易平台的智能助手。你可以帮助用户寻找商品、解答交易问题或者进行日常闲聊。\n");
            systemPromptBuilder.append("如果用户的提问与寻找商品或买卖相关，我已经为你查到了以下几款平台现有的商品，你只需要顺着这些商品简短地做个推荐即可（不要自己编造不存在的商品）。\n");
            systemPromptBuilder.append("如果用户的提问与商品无关（比如日常问候、闲聊等），请直接友好地回复，不需要强行推荐商品。\n");
            
            if (!relevantItemDetails.isEmpty()) {
                log.info("【AI 助手】步骤 2.1: 发现相关商品，正在将商品信息注入 Prompt 上下文...");
                systemPromptBuilder.append("【相关商品上下文开始】\n");
                for (int i = 0; i < relevantItemDetails.size(); i++) {
                    Map<String, Object> item = relevantItemDetails.get(i);
                    systemPromptBuilder.append(i + 1).append(". [itemId: ").append(item.get("itemId"))
                                       .append("] 标题: ").append(item.get("title"))
                                       .append(", 价格: ").append(item.get("price"))
                                       .append(", 描述: ").append(item.get("description")).append("\n");
                }
                systemPromptBuilder.append("【相关商品上下文结束】\n");
                systemPromptBuilder.append("请根据用户的问题，从中挑选真正相关的商品进行推荐。如果这些商品都与用户问题无关，请不要推荐任何商品。\n");
                systemPromptBuilder.append("你必须在回复的最末尾，用 <<<JSON>>> 和 <<<END>>> 标记包裹一个 JSON 块，格式如下：\n");
                systemPromptBuilder.append("<<<JSON>>>{\"text\": \"你的回复内容\", \"recommendIds\": [商品id1, 商品id2]}<<<END>>>\n");
                systemPromptBuilder.append("其中 recommendIds 是你认为值得推荐的商品 itemId 数组。如果不需要推荐任何商品，返回空数组：\"recommendIds\": []\n");
            } else {
                log.info("【AI 助手】步骤 2.1: 未发现相关商品，跳过上下文注入。");
            }

            // 3. 构建包含历史记录的消息列表
            log.info("【AI 助手】步骤 3: 组装多轮对话历史...");
            List<org.springframework.ai.chat.messages.Message> messageList = new java.util.ArrayList<>();
            messageList.add(new SystemMessage(systemPromptBuilder.toString()));
            
            if (history != null && !history.isEmpty()) {
                // 为了避免上下文过长，只取最近的 6 条（3轮对话）
                int startIndex = Math.max(0, history.size() - 6);
                log.info("【AI 助手】步骤 3.1: 截取最近的 {} 条历史记录加入上下文", history.size() - startIndex);
                for (int i = startIndex; i < history.size(); i++) {
                    Map<String, String> msg = history.get(i);
                    String role = msg.get("role");
                    String content = msg.get("content");
                    if ("user".equals(role)) {
                        messageList.add(new UserMessage(content));
                    } else if ("ai".equals(role)) {
                        messageList.add(new org.springframework.ai.chat.messages.AssistantMessage(content));
                    }
                }
            }
            messageList.add(new UserMessage(userMessage));

            Prompt prompt = new Prompt(messageList);

            log.info("【AI 助手】步骤 4: 开始调用 DeepSeek 大模型进行推理...");
            String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            log.info("【AI 助手】步骤 4 完成: DeepSeek 返回成功。响应长度: {} 字符", aiResponse.length());
            log.debug("【AI 助手】大模型返回内容: {}", aiResponse);

            // 5. 解析 LLM 返回的 JSON，提取推荐商品 ID
            String responseText = aiResponse;
            List<Map<String, Object>> filteredItems = List.of();
            try {
                int jsonStart = aiResponse.indexOf("<<<JSON>>>");
                int jsonEnd = aiResponse.indexOf("<<<END>>>");
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    String jsonStr = aiResponse.substring(jsonStart + 10, jsonEnd).trim();
                    log.info("【AI 助手】步骤 5: 解析 LLM 返回的 JSON: {}", jsonStr);
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> parsed = mapper.readValue(jsonStr, Map.class);
                    responseText = (String) parsed.get("text");
                    List<?> recommendIds = (List<?>) parsed.get("recommendIds");
                    if (recommendIds != null && !recommendIds.isEmpty()) {
                        java.util.Set<Long> idSet = recommendIds.stream()
                                .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.parseLong(String.valueOf(id)))
                                .collect(Collectors.toSet());
                        filteredItems = relevantItemDetails.stream()
                                .filter(item -> {
                                    Object itemId = item.get("itemId");
                                    if (itemId == null) return false;
                                    Long id = itemId instanceof Number ? ((Number) itemId).longValue() : Long.parseLong(String.valueOf(itemId));
                                    return idSet.contains(id);
                                })
                                .collect(Collectors.toList());
                    }
                    log.info("【AI 助手】步骤 5 完成: LLM 推荐了 {} 个商品", filteredItems.size());
                } else {
                    log.info("【AI 助手】步骤 5: LLM 未返回 JSON 标记，跳过商品过滤");
                }
            } catch (Exception e) {
                log.warn("【AI 助手】步骤 5: 解析 LLM JSON 失败，降级处理", e);
                responseText = aiResponse.replaceAll("<<<JSON>>>.*?<<<END>>>", "").trim();
            }

            // 6. 返回给前端
            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("text", responseText);
            responseData.put("items", filteredItems);
            
            log.info("【AI 助手】全流程结束，准备返回结果给前端。");
            return Result.success(responseData);
        } catch (Exception e) {
            log.error("【AI 助手】严重错误: 聊天流程执行失败!", e);
            return Result.error("AI 助手开小差了，请稍后再试");
        }
    }
}