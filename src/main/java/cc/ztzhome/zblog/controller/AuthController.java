package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.ChangePasswordDto;
import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.service.impl.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/public/login")
    public ResponseModel<LoginVo> userLogin(@RequestBody LoginDto loginDto){
        System.out.println("邮箱："+loginDto.getEmail()+"\t 密码："+loginDto.getPassword());
        return authService.userLogin(loginDto);
    }

    @PostMapping("/public/register")
    public ResponseModel<LoginVo> userRegister(@Valid @RequestBody RegisterDto rDto){
        return authService.userRegister(rDto);
    }

    @PostMapping("/user/password")
    public ResponseModel<Void> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
    }

    @PostMapping("/user/profile")
    public ResponseModel<UserVo> updateProfile(@Valid @RequestBody UpdateUserDto dto,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.updateProfile(userId, dto);
    }

    @PostMapping("/user/avatar")
    public ResponseModel<UserVo> updateAvatar(@RequestParam("file") MultipartFile file,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.updateAvatar(userId, file);
    }

    //注销账号

}
