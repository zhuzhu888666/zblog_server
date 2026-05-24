package cc.ztzhome.zblog.bean.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BatchStatusDto {
    private List<Long> userIds;
    private Integer status;
}
