package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    int insert(ChatMessageEntity message);

    List<ChatMessageEntity> selectByConversationId(@Param("conversationId") Long conversationId);

    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
