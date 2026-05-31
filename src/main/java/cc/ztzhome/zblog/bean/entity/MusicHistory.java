package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class MusicHistory {
    private long historyId;
    private long userId;
    private long musicId;
    private LocalDateTime playTime;
}
