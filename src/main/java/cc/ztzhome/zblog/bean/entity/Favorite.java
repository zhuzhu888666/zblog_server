package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Favorite {
    private long favoriteId;
    private long userId;
    private long articleId;
    private LocalDateTime createTime = LocalDateTime.now();
}
