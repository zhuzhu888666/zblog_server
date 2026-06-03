package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ChatMessageEntity {
    private Long messageId;
    private Long conversationId;
    private String role;
    private String content;
    private String imageUrl;
    private LocalDateTime createTime = LocalDateTime.now();
}
