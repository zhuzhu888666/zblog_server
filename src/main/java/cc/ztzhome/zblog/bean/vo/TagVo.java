package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TagVo {
    private long tagId;
    private String name;
    private String icon;
    private String keywords;
    private int articleCount;
    private LocalDateTime createTime;
}
