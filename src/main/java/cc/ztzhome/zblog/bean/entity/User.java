package cc.ztzhome.zblog.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class User {
    private long userId;
    private String email;
    private String password;
    private int role;
    private String nickname;
    private int status=0;
    private String userAvatar;
    private int gender;
    private LocalDateTime createTime=LocalDateTime.now();
    private LocalDate birthday;
    private String introduction;
}
