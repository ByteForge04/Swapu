package com.swapu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeepSeekUtils {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekUtils.class);

    private final ChatClient chatClient;

    @Autowired
    public DeepSeekUtils(@org.springframework.beans.factory.annotation.Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 判断物品是否允许上架
     * @param title 物品标题
     * @param description 物品描述
     * @return 返回 JSON 格式结果：{"allow": true|false, "reason": "如果拒绝，返回原因"}
     */
    public String checkItemPublish(String title, String description) {
        String systemPrompt = "你是一个二手交易平台的智能审核助手。请严格根据用户提供的物品【标题】和【描述】，判断该物品是否允许上架。\n" +
                "禁止上架的物品包括但不限于：\n" +
                "1. 违禁品：毒品、枪支弹药、管制刀具、易燃易爆品等；\n" +
                "2. 违法服务：代写论文、代考、色情服务等；\n" +
                "3. 活体动物；\n" +
                "4. 医疗器械及处方药；\n" +
                "5. 包含明显的引流广告或欺诈信息的商品。\n\n" +
                "请只输出 JSON 格式，格式如下：\n" +
                "{\n" +
                "  \"allow\": true或false,\n" +
                "  \"reason\": \"如果allow为false，请简述拒绝原因；如果为true，留空或写无。\"\n" +
                "}\n" +
                "不要输出任何其他多余的文字！";
                
        String userPrompt = "标题: " + title + "\n描述: " + description;
        
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));
            log.info("Calling DeepSeek AI to check item publish. Item Title: {}", title);
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            log.debug("DeepSeek response for item publish: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error calling DeepSeek API for item publish check", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 判断评价内容是否合规
     * @param content 评价内容
     * @return 返回 JSON 格式结果：{"allow": true|false, "reason": "如果拒绝，返回原因"}
     */
    public String checkComment(String content) {
        String systemPrompt = "你是一个二手交易平台的智能审核助手。请严格根据用户提供的【评价内容】，判断该评价是否合规。\n" +
                "违规的评价包括但不限于：\n" +
                "1. 包含辱骂、人身攻击、歧视等恶意言论；\n" +
                "2. 包含色情、涉政、违禁品等违法违规内容；\n" +
                "3. 包含明显的引流广告、外部链接、微信号、QQ号等站外联系方式；\n" +
                "4. 包含无意义的乱码或恶意刷屏内容。\n\n" +
                "请只输出 JSON 格式，格式如下：\n" +
                "{\n" +
                "  \"allow\": true或false,\n" +
                "  \"reason\": \"如果allow为false，请简述拒绝原因；如果为true，留空或写无。\"\n" +
                "}\n" +
                "不要输出任何其他多余的文字！";

        String userPrompt = "评价内容: " + content;

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));

            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            return response.trim();
        } catch (Exception e) {
            log.error("DeepSeek AI 评价审核调用失败", e);
            throw new RuntimeException("AI调用失败", e);
        }
    }

    /**
     * 审核举报是否合理
     * @param itemTitle 举报目标的物品标题
     * @param itemDesc 举报目标的物品描述
     * @param reportReason 用户提交的举报理由
     * @return 返回 JSON 格式结果：{"valid": true|false, "reason": "处理建议或理由"}
     */
    public String checkReportReasonable(String itemTitle, String itemDesc, String reportReason) {
        String systemPrompt = "你是一个二手交易平台的举报处理审核助手。用户对一个物品发起了举报，请根据物品的【标题】、【描述】以及用户的【举报理由】，判断该举报是否合理。\n" +
                "如果物品确实违反了平台规则（如违禁品、色情、诈骗等），或者用户的举报理由与物品信息存在明显的违规对应关系，判定为合理（true）。\n" +
                "如果是恶意举报、无端指责，或物品信息正常，判定为不合理（false）。\n\n" +
                "请只输出 JSON 格式，格式如下：\n" +
                "{\n" +
                "  \"valid\": true或false,\n" +
                "  \"reason\": \"简述判断理由及建议的处理结果。\"\n" +
                "}\n" +
                "不要输出任何其他多余的文字！";

        String userPrompt = "被举报物品标题: " + itemTitle + "\n" +
                            "被举报物品描述: " + itemDesc + "\n" +
                            "举报理由: " + reportReason;
                            
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));
            log.info("Calling DeepSeek AI to check report reason. Item Title: {}", itemTitle);
            String response = chatClient.call(prompt).getResult().getOutput().getContent();
            log.debug("DeepSeek response for report check: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error calling DeepSeek API for report check", e);
            return "Error: " + e.getMessage();
        }
    }
}