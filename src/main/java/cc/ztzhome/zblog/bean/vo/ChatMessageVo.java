package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ChatMessageVo {
    private long id;                 // 消息ID
    private long conversationId;     // 所属对话ID
    private String role;             // 消息角色：user（用户）| assistant（AI）
    private String content;          // 消息文本内容
    private String imageUrl;         // AI生成的图片URL（前端通过此字段渲染图片）
    private LocalDateTime createdAt; // 消息创建时间
}
