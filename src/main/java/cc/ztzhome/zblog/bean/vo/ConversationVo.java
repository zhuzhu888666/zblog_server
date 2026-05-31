package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ConversationVo {
    private long id;
    private String title;
    private String lastMessage;
    private LocalDateTime updatedAt;
    private int messageCount;
}
