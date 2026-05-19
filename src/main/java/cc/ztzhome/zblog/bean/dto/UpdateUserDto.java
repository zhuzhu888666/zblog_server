package cc.ztzhome.zblog.bean.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UpdateUserDto {

    @Size(min = 1, max = 30, message = "昵称长度需在1-30位之间")
    private String nickname;

    /**
     * 0-未设置, 1-男, 2-女
     */
    private Integer gender;

    @Size(max = 200, message = "个人简介不能超过200字")
    private String introduction;

    private LocalDate birthday;
}
