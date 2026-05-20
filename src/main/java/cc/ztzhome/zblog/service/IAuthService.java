package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.bean.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;

public interface IAuthService {
    ResponseModel<LoginVo> userLogin(LoginDto loginDto);

    ResponseModel<LoginVo> userRegister(RegisterDto rDto);

    ResponseModel<Void> changePassword(Long userId, String oldPassword, String newPassword);

    ResponseModel<UserVo> updateProfile(Long userId, UpdateUserDto dto);

    ResponseModel<UserVo> updateAvatar(Long userId, MultipartFile file);
}
