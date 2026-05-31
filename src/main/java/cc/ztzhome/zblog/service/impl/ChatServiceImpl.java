package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.ChatConversation;
import cc.ztzhome.zblog.bean.entity.ChatMessageEntity;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ChatMessageVo;
import cc.ztzhome.zblog.bean.vo.ConversationVo;
import cc.ztzhome.zblog.mapper.ChatConversationMapper;
import cc.ztzhome.zblog.mapper.ChatMessageMapper;
import cc.ztzhome.zblog.service.IChatService;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ChatServiceImpl implements IChatService {

    @Autowired
    private ChatConversationMapper conversationMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    @Qualifier("deepseekChatModel")
    private ChatModel chatModel;

    @Override
    public ResponseModel<ChatMessageVo> sendMessage(Long userId, Long conversationId, String message) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (message == null || message.isBlank()) {
            return ResponseModel.error("消息不能为空");
        }

        ChatConversation conversation;

        if (conversationId == null || conversationId == 0) {
            conversation = new ChatConversation();
            conversation.setUserId(userId);
            conversation.setTitle(generateTitle(message));
            conversationMapper.insert(conversation);
            conversationId = conversation.getConversationId();
        } else {
            conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                return ResponseModel.notFound();
            }
            if (!conversation.getUserId().equals(userId)) {
                return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权访问此会话");
            }
        }

        // Save user message
        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(message.trim());
        chatMessageMapper.insert(userMsg);

        // Load conversation history and build prompt
        List<ChatMessageEntity> history = chatMessageMapper.selectByConversationId(conversationId);
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是一个有帮助的AI助手，请用中文回答用户的问题。"));
        for (ChatMessageEntity msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Call DeepSeek AI
        try {
            ChatResponse response = chatModel.call(new Prompt(messages));
            String aiContent = Objects.requireNonNull(response.getResult()).getOutput().getText();

            ChatMessageEntity aiMsg = new ChatMessageEntity();
            aiMsg.setConversationId(conversationId);
            aiMsg.setRole("assistant");
            aiMsg.setContent(aiContent);
            chatMessageMapper.insert(aiMsg);

            conversationMapper.updateTime(conversationId);

            return ResponseModel.success(toMessageVo(aiMsg));
        } catch (Exception e) {
            log.error("AI API call failed for conversation {}", conversationId, e);
            return ResponseModel.error(ResponseModel.CODE_INTERNAL_ERROR, "AI服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public ResponseModel<List<ConversationVo>> listConversations(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<ConversationVo> conversations = conversationMapper.selectListByUserId(userId);
        return ResponseModel.success(conversations);
    }

    @Override
    public ResponseModel<List<ChatMessageVo>> getConversationMessages(Long userId, Long conversationId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (conversationId == null) {
            return ResponseModel.error("会话ID不能为空");
        }
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return ResponseModel.notFound();
        }
        if (!conversation.getUserId().equals(userId)) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权访问此会话");
        }
        List<ChatMessageEntity> messages = chatMessageMapper.selectByConversationId(conversationId);
        List<ChatMessageVo> voList = new ArrayList<>();
        for (ChatMessageEntity msg : messages) {
            voList.add(toMessageVo(msg));
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<Void> deleteConversation(Long userId, Long conversationId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (conversationId == null) {
            return ResponseModel.error("会话ID不能为空");
        }
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return ResponseModel.notFound();
        }
        if (!conversation.getUserId().equals(userId)) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权删除此会话");
        }
        chatMessageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
        return ResponseModel.success("删除成功");
    }

    private String generateTitle(String message) {
        if (message == null || message.isBlank()) {
            return "新对话";
        }
        String trimmed = message.trim();
        if (trimmed.length() <= 30) {
            return trimmed;
        }
        String title = trimmed.substring(0, 30);
        int lastBreak = Math.max(
                title.lastIndexOf('。'), Math.max(  // 。
                title.lastIndexOf('，'), Math.max(  // ，
                title.lastIndexOf('.'), Math.max(
                title.lastIndexOf(','), Math.max(
                title.lastIndexOf(' '), title.lastIndexOf('\n')))))
        );
        if (lastBreak > 15) {
            title = title.substring(0, lastBreak);
        }
        return title + "...";
    }

    private ChatMessageVo toMessageVo(ChatMessageEntity entity) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(entity.getMessageId());
        vo.setConversationId(entity.getConversationId());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setCreatedAt(entity.getCreateTime());
        return vo;
    }
}
