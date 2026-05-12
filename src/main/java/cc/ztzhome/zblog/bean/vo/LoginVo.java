package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginVo {
    private String token;
    private UserVo userVo;
}
