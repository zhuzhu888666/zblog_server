package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ChatMessageEntity {
    private Long messageId;          // 消息ID（主键，自增）
    private Long conversationId;     // 所属对话ID
    private String role;             // 消息角色：user（用户）| assistant（AI）
    private String content;          // 消息文本内容
    private String imageUrl;         // AI生成的图片URL（仅图片处理模式使用，预签名链接）
    private LocalDateTime createTime = LocalDateTime.now();  // 消息创建时间
}
