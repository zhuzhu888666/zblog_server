package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ChatMessageVo;
import cc.ztzhome.zblog.bean.vo.ConversationVo;

import java.util.List;

public interface IChatService {
    ResponseModel<ChatMessageVo> sendMessage(Long userId, Long conversationId, String message);

    ResponseModel<List<ConversationVo>> listConversations(Long userId);

    ResponseModel<List<ChatMessageVo>> getConversationMessages(Long userId, Long conversationId);

    ResponseModel<Void> deleteConversation(Long userId, Long conversationId);
}
