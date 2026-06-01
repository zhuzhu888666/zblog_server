package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ArticleVo {
    private long articleId;
    private long userId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String content;
    private String coverUrl;
    private String articleType;
    private int status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int likeCount;
}
