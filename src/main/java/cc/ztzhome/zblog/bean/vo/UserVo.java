package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserVo {
    private long userId;
    private String email;
    private int role;
    private String nickname;
    private int gender;
    private String introduction;
}