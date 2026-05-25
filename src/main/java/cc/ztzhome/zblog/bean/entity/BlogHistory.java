package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BlogHistory {
    private long historyId;
    private long userId;
    private long articleId;
    private LocalDateTime createTime = LocalDateTime.now();
}
