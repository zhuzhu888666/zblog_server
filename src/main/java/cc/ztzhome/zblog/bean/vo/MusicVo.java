package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MusicVo {
    private long musicId;
    private String title;
    private long artistId;
    private String artist;
    private String duration;
    private String audioUrl;
    private String coverUrl;
    private String genre;
    private LocalDateTime releaseTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long favoriteCount;
}
