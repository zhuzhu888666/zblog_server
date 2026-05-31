package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Playlist {
    private long playlistId;
    private long userId;
    private String name;
    private String description;
    private String coverPath;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int deleted;
}
