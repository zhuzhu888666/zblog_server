package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CommentVo {
    private long commentId;
    private long articleId;
    private long userId;
    private Long parentId;
    private Long replyToUserId;
    private String replyToUserName;
    private String authorName;
    private String authorAvatar;
    private String content;
    private LocalDateTime createTime;
}
