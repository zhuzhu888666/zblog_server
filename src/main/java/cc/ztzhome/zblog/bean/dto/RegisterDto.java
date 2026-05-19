package cc.ztzhome.zblog.bean.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class RegisterDto {
    private String email;
    private String password;
    private int role;
    private String nickname;
    private int gender;
    private String introduction;
    private LocalDate birthday;
}
