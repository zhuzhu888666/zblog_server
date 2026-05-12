package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private long userId;
    private String email;
    private String password;
    private int role;
    private String nickname;
    private int gender;
    private String introduction;
}
