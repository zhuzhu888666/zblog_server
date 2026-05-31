package cc.ztzhome.zblog.bean.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SendMessageDto {
    private Long conversationId;
    private String message;
}
