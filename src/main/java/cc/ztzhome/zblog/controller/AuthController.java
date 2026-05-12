package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.service.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/public/login")
    public ResponseModel<LoginVo> userLogin(LoginDto loginDto){
        return authService.userLogin(loginDto);
    }
}
