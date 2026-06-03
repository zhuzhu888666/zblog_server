package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.SendMessageDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ChatMessageVo;
import cc.ztzhome.zblog.bean.vo.ConversationVo;
import cc.ztzhome.zblog.service.IChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AiChatController {

    @Autowired
    private IChatService chatService;

    @PostMapping("/chat/send")
    public ResponseModel<ChatMessageVo> sendMessage(
            @RequestBody SendMessageDto dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.sendMessage(userId, dto.getConversationId(), dto.getMessage(), dto.getModel());
    }

    @GetMapping("/chat/conversations")
    public ResponseModel<List<ConversationVo>> listConversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.listConversations(userId);
    }

    @GetMapping("/chat/conversation/{id}")
    public ResponseModel<List<ChatMessageVo>> getConversationMessages(
            @PathVariable("id") Long conversationId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.getConversationMessages(userId, conversationId);
    }

    @DeleteMapping("/chat/conversation/{id}")
    public ResponseModel<Void> deleteConversation(
            @PathVariable("id") Long conversationId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.deleteConversation(userId, conversationId);
    }
}
