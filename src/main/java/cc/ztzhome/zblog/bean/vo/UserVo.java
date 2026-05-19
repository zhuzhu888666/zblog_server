package cc.ztzhome.zblog.bean.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class UserVo {
    private long userId;
    private String email;
    private int role;
    private String nickname;
    private int gender;
    private String introduction;
    private int status;
    private String userAvatar;
    private LocalDateTime createTime;
    private LocalDate birthday;
}