package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.service.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseModel userRegister(@RequestBody RegisterDto rDto){
        return authService.userRegister(rDto);
    }

    //修改账户信息


    //注销账号

}
