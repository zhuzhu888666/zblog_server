package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.ChatConversation;
import cc.ztzhome.zblog.bean.vo.ConversationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatConversationMapper {
    int insert(ChatConversation conversation);

    ChatConversation selectById(@Param("conversationId") Long conversationId);

    List<ConversationVo> selectListByUserId(@Param("userId") Long userId);

    int updateTitle(@Param("conversationId") Long conversationId, @Param("title") String title);

    int updateTime(@Param("conversationId") Long conversationId);

    int deleteById(@Param("conversationId") Long conversationId);
}
