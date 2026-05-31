package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class PlaylistMusic {
    private long id;
    private long playlistId;
    private long musicId;
    private int sortOrder;
    private LocalDateTime createTime;
}
