package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.ChatConversation;
import cc.ztzhome.zblog.bean.entity.ChatMessageEntity;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ChatMessageVo;
import cc.ztzhome.zblog.bean.vo.ConversationVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.ChatConversationMapper;
import cc.ztzhome.zblog.mapper.ChatMessageMapper;
import cc.ztzhome.zblog.service.IChatService;
import cc.ztzhome.zblog.service.RustFsService;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 聊天服务实现类
 * 提供对话管理、消息发送、历史记录等核心功能
 */
@Slf4j
@Service
public class ChatServiceImpl implements IChatService {

    @Autowired
    private ChatConversationMapper conversationMapper;  // 对话记录Mapper

    @Autowired
    private ChatMessageMapper chatMessageMapper;        // 消息记录Mapper

    @Autowired
    @Qualifier("deepSeekChatModel")
    private ChatModel deepSeekChatModel;  // DeepSeek模型（聊天/帮助）

    @Autowired
    private RustFsService rustFsService;

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    /**
     * 发送消息并获取AI回复
     *
     * @param userId         当前用户ID
     * @param conversationId 对话ID（可为空，表示新建对话）
     * @param message        用户发送的消息内容
     * @return 包含AI回复消息的响应对象
     */
    @Override
    public ResponseModel<ChatMessageVo> sendMessage(Long userId, Long conversationId, String message, String model) {
        // 用户未登录校验
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        // 消息内容不能为空
        if (message == null || message.isBlank()) {
            return ResponseModel.error("消息不能为空");
        }

        ChatConversation conversation;

        // 新建对话或使用已有对话
        if (conversationId == null || conversationId == 0) {
            // 创建新对话
            conversation = new ChatConversation();
            conversation.setUserId(userId);
            conversation.setTitle(generateTitle(message));  // 根据首条消息生成标题
            conversationMapper.insert(conversation);
            conversationId = conversation.getConversationId();
        } else {
            // 校验已有对话的归属权
            conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                return ResponseModel.notFound();
            }
            if (!conversation.getUserId().equals(userId)) {
                return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权访问此会话");
            }
        }

        // 保存用户消息
        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(message.trim());
        chatMessageMapper.insert(userMsg);

        // 加载对话历史并构建提示（Prompt）
        List<ChatMessageEntity> history = chatMessageMapper.selectByConversationId(conversationId);
        List<Message> messages = new ArrayList<>();
        // 添加系统角色设定（AI助手身份）
        messages.add(new SystemMessage("你是一个有帮助的博客AI助手，身份是小助手，请用中文回答用户的问题。"));
        // 将历史消息转换为Spring AI消息格式
        for (ChatMessageEntity msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 图片处理模型走图像生成流程
        if ("image".equals(model)) {
            return handleImageGeneration(conversationId, message.trim(), conversation);
        }

        // 根据模型类型选择对应的AI模型
        ChatModel selectedModel = deepSeekChatModel;

        // 调用AI模型获取回复
        try {
            ChatResponse response = selectedModel.call(new Prompt(messages));
            String aiContent = Objects.requireNonNull(response.getResult()).getOutput().getText();

            // 保存AI回复消息
            ChatMessageEntity aiMsg = new ChatMessageEntity();
            aiMsg.setConversationId(conversationId);
            aiMsg.setRole("assistant");
            aiMsg.setContent(aiContent);
            chatMessageMapper.insert(aiMsg);

            // 更新对话的最后活跃时间
            conversationMapper.updateTime(conversationId);

            return ResponseModel.success(toMessageVo(aiMsg));
        } catch (Exception e) {
            log.error("AI API call failed for conversation {}", conversationId, e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取用户的所有对话列表
     *
     * @param userId 用户ID
     * @return 对话列表响应
     */
    @Override
    public ResponseModel<List<ConversationVo>> listConversations(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<ConversationVo> conversations = conversationMapper.selectListByUserId(userId);
        return ResponseModel.success(conversations);
    }

    /**
     * 获取指定对话的所有历史消息
     *
     * @param userId         用户ID（用于权限校验）
     * @param conversationId 对话ID
     * @return 消息列表响应
     */
    @Override
    public ResponseModel<List<ChatMessageVo>> getConversationMessages(Long userId, Long conversationId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (conversationId == null) {
            return ResponseModel.error("会话ID不能为空");
        }
        // 校验对话存在且属于当前用户
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return ResponseModel.notFound();
        }
        if (!conversation.getUserId().equals(userId)) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权访问此会话");
        }
        // 查询消息并转换为VO对象
        List<ChatMessageEntity> messages = chatMessageMapper.selectByConversationId(conversationId);
        List<ChatMessageVo> voList = new ArrayList<>();
        for (ChatMessageEntity msg : messages) {
            voList.add(toMessageVo(msg));
        }
        return ResponseModel.success(voList);
    }

    /**
     * 删除整个对话（同时删除其下所有消息）
     *
     * @param userId         用户ID
     * @param conversationId 对话ID
     * @return 操作结果响应
     */
    @Override
    public ResponseModel<Void> deleteConversation(Long userId, Long conversationId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (conversationId == null) {
            return ResponseModel.error("会话ID不能为空");
        }
        // 校验对话归属权
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return ResponseModel.notFound();
        }
        if (!conversation.getUserId().equals(userId)) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权删除此会话");
        }
        // 删除消息及对话记录
        chatMessageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
        return ResponseModel.success("删除成功");
    }

    /**
     * 处理图片生成请求（阿里云百炼 DashScope SDK 原生调用）
     * 1. 调用通义万相API获取临时图片URL
     * 2. 下载图片并上传到RustFS S3
     * 3. 生成预签名URL并保存消息
     */
    private ResponseModel<ChatMessageVo> handleImageGeneration(
            Long conversationId, String prompt, ChatConversation conversation) {

        // 1. 调用通义万相图像生成API
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(dashscopeApiKey)
                .model("wanx2.0-t2i-turbo")
                .prompt(prompt)
                .n(1)
                .size("1024*1024")
                .build();

        ImageSynthesisResult result;
        try {
            result = new ImageSynthesis().call(param);
        } catch (NoApiKeyException e) {
            log.error("DashScope API key not configured", e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "图片生成服务未配置API密钥");
        } catch (ApiException e) {
            log.error("DashScope image generation API call failed", e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "图片生成失败，请稍后再试");
        }

        String tempImageUrl = result.getOutput().getResults().get(0).get("url");
        if (tempImageUrl == null || tempImageUrl.isBlank()) {
            log.error("Image generation returned empty URL");
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "图片生成失败，未返回图片");
        }

        // 2. 下载生成的图片
        byte[] imageBytes;
        try {
            imageBytes = new RestTemplate().getForObject(URI.create(tempImageUrl), byte[].class);
        } catch (Exception e) {
            log.error("Failed to download generated image from {}", tempImageUrl, e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "下载生成的图片失败");
        }

        if (imageBytes == null || imageBytes.length == 0) {
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "下载的图片为空");
        }

        // 3. 上传到RustFS S3
        String objectKey = AppConstants.AI_IMAGE_PATH + UUID.randomUUID() + ".png";
        try {
            rustFsService.upload(objectKey, new ByteArrayInputStream(imageBytes),
                    imageBytes.length, "image/png");
        } catch (Exception e) {
            log.error("Failed to upload generated image to RustFS: {}", objectKey, e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "图片存储失败");
        }

        // 4. 生成预签名URL
        String presignedUrl;
        try {
            presignedUrl = rustFsService.presignedGetUrl(objectKey, AppConstants.URL_TIMEOUT);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for {}", objectKey, e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "生成图片链接失败");
        }

        // 5. 保存AI回复消息
        ChatMessageEntity aiMsg = new ChatMessageEntity();
        aiMsg.setConversationId(conversationId);
        aiMsg.setRole("assistant");
        aiMsg.setContent("[生成的图片]");
        aiMsg.setImageUrl(presignedUrl);
        chatMessageMapper.insert(aiMsg);

        conversationMapper.updateTime(conversationId);

        return ResponseModel.success(toMessageVo(aiMsg));
    }

    /**
     * 根据首条消息内容生成对话标题
     * 规则：长度不超过30字符，优先在标点或空格处截断，超长则添加省略号
     *
     * @param message 用户首条消息
     * @return 生成的标题
     */
    private String generateTitle(String message) {
        if (message == null || message.isBlank()) {
            return "新对话";
        }
        String trimmed = message.trim();
        if (trimmed.length() <= 30) {
            return trimmed;
        }
        String title = trimmed.substring(0, 30);
        // 查找最后一个合适的中文/英文标点或空白符进行截断
        int lastBreak = Math.max(
                title.lastIndexOf('。'), Math.max(  // 中文句号
                        title.lastIndexOf('，'), Math.max(  // 中文逗号
                                title.lastIndexOf('.'), Math.max(
                                        title.lastIndexOf(','), Math.max(
                                                title.lastIndexOf(' '), title.lastIndexOf('\n')))))
        );
        // 如果截断位置靠后（大于15字符），则在此处断开
        if (lastBreak > 15) {
            title = title.substring(0, lastBreak);
        }
        return title + "...";
    }

    /**
     * 将消息实体对象转换为视图对象（VO）
     *
     * @param entity 消息实体
     * @return 视图对象
     */
    private ChatMessageVo toMessageVo(ChatMessageEntity entity) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(entity.getMessageId());
        vo.setConversationId(entity.getConversationId());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setImageUrl(entity.getImageUrl());
        vo.setCreatedAt(entity.getCreateTime());
        return vo;
    }
}