package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ChatMessageVo {
    private long id;
    private long conversationId;
    private String role;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
}
