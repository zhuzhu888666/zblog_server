package cc.ztzhome.zblog.bean.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCommentDto {
    private Long articleId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
}
