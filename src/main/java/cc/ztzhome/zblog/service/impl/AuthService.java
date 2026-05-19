package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IAuthService;
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

    /**
     * 用户登录：校验邮箱密码，生成 JWT Token 并返回用户信息
     */
    @Override
    public ResponseModel<LoginVo> userLogin(LoginDto loginDto) {
        // 1. 参数校验
        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱和密码不能为空");
        }

        // 2. 查询用户
        User user = userMapper.selectAllByEmail(email);
        if (user == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱或密码错误");
        }

        // 3. 校验密码
        if (!BCryptUtil.match(password, user.getPassword())) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "邮箱或密码错误");
        }

        // 4. 生成 Token
        boolean isRememberMe = loginDto.getIsLongLogin() == 1;
        String token = jwtUtil.generateToken(user.getUserId(), String.valueOf(user.getRole()), isRememberMe);

        // 5. 构建响应
        UserVo userVo = new UserVo();
        userVo.setUserId(user.getUserId());
        userVo.setEmail(user.getEmail());
        userVo.setRole(user.getRole());
        userVo.setNickname(user.getNickname());
        userVo.setGender(user.getGender());
        userVo.setIntroduction(user.getIntroduction());
        userVo.setStatus(user.getStatus());
        userVo.setUserAvatar(user.getUserAvatar());
        userVo.setCreateTime(user.getCreateTime());
        userVo.setBirthday(user.getBirthday());

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUserVo(userVo);

        return ResponseModel.success("登录成功", loginVo);
    }

    @Override
    public ResponseModel<LoginVo> userRegister(RegisterDto rDto) {
        String email = rDto.getEmail();
        String password = rDto.getPassword();
        String confirmPassword = rDto.getConfirmPassword();

        // 1. 参数校验（防御性编程，控制器层已有 @Valid）
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

        // 2. 检查邮箱是否已注册
        if (userMapper.existsByEmail(email)) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "该邮箱已被注册");
        }

        // 3. 构建用户实体（role 强制为 1，不允许客户端设置）
        User user = new User();
        user.setEmail(email);
        user.setPassword(BCryptUtil.encrypt(password));
        user.setRole(1);
        user.setNickname(rDto.getNickname());
        user.setGender(rDto.getGender());
        user.setIntroduction(rDto.getIntroduction());
        user.setBirthday(rDto.getBirthday());

        // 4. 插入数据库
        int result = userMapper.insertUser(user);
        if (result <= 0) {
            return ResponseModel.serverError();
        }

        // 5. 注册成功，生成 Token 并返回用户信息（自动登录）
        String token = jwtUtil.generateToken(user.getUserId(), String.valueOf(user.getRole()));

        UserVo userVo = new UserVo();
        userVo.setUserId(user.getUserId());
        userVo.setEmail(user.getEmail());
        userVo.setRole(user.getRole());
        userVo.setNickname(user.getNickname());
        userVo.setGender(user.getGender());
        userVo.setIntroduction(user.getIntroduction());
        userVo.setStatus(user.getStatus());
        userVo.setUserAvatar(user.getUserAvatar());
        userVo.setCreateTime(user.getCreateTime());
        userVo.setBirthday(user.getBirthday());

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUserVo(userVo);

        return ResponseModel.success("注册成功", loginVo);
    }
}
