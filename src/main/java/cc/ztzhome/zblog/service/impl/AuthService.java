package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IAuthService;
import cc.ztzhome.zblog.service.IUserService;
import cc.ztzhome.zblog.utils.BCryptUtil;
import cc.ztzhome.zblog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserService userService;

    @Override
    public ResponseModel<LoginVo> userLogin(LoginDto loginDto) {
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱和密码不能为空");
        }

        User user = userMapper.selectAllByEmail(email);
        if (user == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱或密码错误");
        }

        if (!BCryptUtil.match(password, user.getPassword())) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱或密码错误");
        }

        boolean isRememberMe = loginDto.getIsLongLogin() == 1;
        String token = jwtUtil.generateToken(user.getUserId(), String.valueOf(user.getRole()), isRememberMe);

        UserVo userVo = userService.toUserVo(user);

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUserVo(userVo);

        return ResponseModel.success("登录成功", loginVo);
    }

    @Override
    public ResponseModel<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (oldPassword == null || oldPassword.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "原密码不能为空");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "新密码长度不能少于6位");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "用户不存在");
        }

        if (!BCryptUtil.match(oldPassword, user.getPassword())) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "原密码错误");
        }

        if (BCryptUtil.match(newPassword, user.getPassword())) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "新密码不能与原密码相同");
        }

        String hashedPassword = BCryptUtil.encrypt(newPassword);
        int result = userMapper.updatePassword(userId, hashedPassword);
        if (result <= 0) {
            return ResponseModel.serverError();
        }

        return ResponseModel.success("密码修改成功");
    }

    @Override
    public ResponseModel<LoginVo> userRegister(RegisterDto rDto) {
        String email = rDto.getEmail();
        String password = rDto.getPassword();
        String confirmPassword = rDto.getConfirmPassword();

        if (email == null || email.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱不能为空");
        }
        if (password == null || password.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "密码不能为空");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "确认密码不能为空");
        }
        if (!password.equals(confirmPassword)) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "两次输入的密码不一致");
        }
        if (password.length() < 6) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "密码长度不能少于6位");
        }

        if (userMapper.existsByEmail(email)) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "该邮箱已被注册");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(BCryptUtil.encrypt(password));
        user.setRole(1);
        user.setNickname(rDto.getNickname());
        user.setGender(rDto.getGender());
        user.setIntroduction(rDto.getIntroduction());
        user.setBirthday(rDto.getBirthday());

        int result = userMapper.insertUser(user);
        if (result <= 0) {
            return ResponseModel.serverError();
        }

        String token = jwtUtil.generateToken(user.getUserId(), String.valueOf(user.getRole()));

        UserVo userVo = userService.toUserVo(user);

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUserVo(userVo);

        return ResponseModel.success("注册成功", loginVo);
    }
}
