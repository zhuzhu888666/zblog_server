package cc.ztzhome.zblog.bean.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BatchArticleDto {
    private List<Long> articleIds;
    private Integer status;
}
