package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Article {
    private long articleId;
    private long userId;
    private String title;
    private String content;
    private String coverKey;
    private String articleType = "other";
    private int status = 1;
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime;
}
