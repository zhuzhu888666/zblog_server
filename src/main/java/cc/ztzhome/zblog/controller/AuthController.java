package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.ChangePasswordDto;
import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/public/login")
    public ResponseModel<LoginVo> userLogin(@RequestBody LoginDto loginDto) {
        return authService.userLogin(loginDto);
    }

    @PostMapping("/public/register")
    public ResponseModel<LoginVo> userRegister(@Valid @RequestBody RegisterDto rDto) {
        return authService.userRegister(rDto);
    }

    @PostMapping("/user/password")
    public ResponseModel<Void> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
    }

    @PostMapping("/user/logout")
    public ResponseModel<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.logout(userId);
    }
}
