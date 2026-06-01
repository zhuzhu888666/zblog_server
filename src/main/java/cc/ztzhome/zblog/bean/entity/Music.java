package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Music {
    private long musicId;
    private String title;
    private long artistId;
    private String artist;
    private String duration;
    private String filePath;
    private String coverPath;
    private String lyricPath;
    private String genre;
    private LocalDateTime releaseTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int deleted;
    private Long favoriteCount;
}
