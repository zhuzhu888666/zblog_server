package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.service.IAuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {
    @Override
    public ResponseModel<LoginVo> userLogin(LoginDto loginDto) {
        return null;
    }
}
