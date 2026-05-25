package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MusicFavorite {
    private long favoriteId;
    private long userId;
    private long musicId;
    private LocalDateTime createTime = LocalDateTime.now();
}
