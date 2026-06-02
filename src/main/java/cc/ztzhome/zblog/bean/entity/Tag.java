package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Tag {
    private long tagId;
    private String name;
    private String icon;
    private String keywords;
    private LocalDateTime createTime;
    // transient — populated by mapper subquery, not a DB column
    private int articleCount;
    // transient — used by selectByArticleIds to group tags by article
    private Long articleId;
}
