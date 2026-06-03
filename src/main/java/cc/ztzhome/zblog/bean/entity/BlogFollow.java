package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class BlogFollow {
    private long followId;
    private long followerId;
    private long followeeId;
    private LocalDateTime createTime = LocalDateTime.now();
}
