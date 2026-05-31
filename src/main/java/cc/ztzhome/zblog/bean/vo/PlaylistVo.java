package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class PlaylistVo {
    private long playlistId;
    private String name;
    private String description;
    private String coverUrl;
    private int songCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
