package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.dto.LoginDto;
import cc.ztzhome.zblog.bean.dto.RegisterDto;
import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.LoginVo;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IAuthService;
import cc.ztzhome.zblog.service.RustFsService;
import cc.ztzhome.zblog.utils.BCryptUtil;
import cc.ztzhome.zblog.utils.FileTypeUtil;
import cc.ztzhome.zblog.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RustFsService rustFsService;

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

        //删除Redis上的token让其重新登录；

        return ResponseModel.success("密码修改成功");
    }

    @Override
    public ResponseModel<UserVo> updateProfile(Long userId, UpdateUserDto dto) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }

        String nickname = dto.getNickname();
        String introduction = dto.getIntroduction();

        if (nickname != null && nickname.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "昵称不能为空");
        }
        if (introduction != null && introduction.length() > 200) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "个人简介不能超过200字");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "用户不存在");
        }

        int result = userMapper.updateProfile(userId, nickname, dto.getGender(), introduction, dto.getBirthday());
        if (result <= 0) {
            return ResponseModel.serverError();
        }

        User updated = userMapper.selectById(userId);
        UserVo userVo = new UserVo();
        userVo.setUserId(updated.getUserId());
        userVo.setEmail(updated.getEmail());
        userVo.setRole(updated.getRole());
        userVo.setNickname(updated.getNickname());
        userVo.setGender(updated.getGender());
        userVo.setIntroduction(updated.getIntroduction());
        userVo.setStatus(updated.getStatus());
        userVo.setUserAvatar(updated.getUserAvatar());
        userVo.setCreateTime(updated.getCreateTime());
        userVo.setBirthday(updated.getBirthday());

        return ResponseModel.success("资料更新成功", userVo);
    }

    @Override
    public ResponseModel<UserVo> updateAvatar(Long userId, MultipartFile file) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (file == null || file.isEmpty()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "请选择要上传的图片");
        }

        String originalFilename = file.getOriginalFilename();
        if (!"image".equals(FileTypeUtil.getFileType(originalFilename))) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "不支持的图片格式，仅支持 JPG、PNG、GIF、WebP");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "图片大小不能超过 10MB");
        }

        String extension = FileTypeUtil.getFileExtension2(originalFilename);
        String objectKey = AppConstants.USER_AVATAR + userId + extension;

        try {
            rustFsService.upload(objectKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            return ResponseModel.serverError();
        }

        String avatarUrl = rustFsService.presignedGetUrl(objectKey, 10080);
        userMapper.updateAvatar(userId, avatarUrl);

        User updated = userMapper.selectById(userId);
        UserVo userVo = new UserVo();
        userVo.setUserId(updated.getUserId());
        userVo.setEmail(updated.getEmail());
        userVo.setRole(updated.getRole());
        userVo.setNickname(updated.getNickname());
        userVo.setGender(updated.getGender());
        userVo.setIntroduction(updated.getIntroduction());
        userVo.setStatus(updated.getStatus());
        userVo.setUserAvatar(updated.getUserAvatar());
        userVo.setCreateTime(updated.getCreateTime());
        userVo.setBirthday(updated.getBirthday());

        return ResponseModel.success("头像更新成功", userVo);
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
        System.out.println("注册的email:"+email);
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
