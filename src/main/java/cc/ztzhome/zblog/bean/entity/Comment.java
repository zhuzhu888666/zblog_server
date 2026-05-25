package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Comment {
    private long commentId;
    private long articleId;
    private long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private LocalDateTime createTime = LocalDateTime.now();
}
