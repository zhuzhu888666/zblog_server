package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IUserService;
import cc.ztzhome.zblog.service.RustFsService;
import cc.ztzhome.zblog.utils.FileTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private static final long AVATAR_URL_TIMEOUT_MINUTES = AppConstants.URL_TIMEOUT / (60 * 1000);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RustFsService rustFsService;

    @Override
    public UserVo toUserVo(User user) {
        if (user == null) return null;
        UserVo vo = new UserVo();
        vo.setUserId(user.getUserId());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setNickname(user.getNickname());
        vo.setGender(user.getGender());
        vo.setIntroduction(user.getIntroduction());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setBirthday(user.getBirthday());

        String avatarValue = user.getUserAvatar();
        if (avatarValue != null && !avatarValue.isEmpty()) {
            if (isProbablyObjectKey(avatarValue)) {
                try {
                    vo.setUserAvatar(rustFsService.presignedGetUrl(avatarValue, AVATAR_URL_TIMEOUT_MINUTES));
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for avatar key: {}", avatarValue, e);
                    vo.setUserAvatar(null);
                }
            } else {
                vo.setUserAvatar(avatarValue);
            }
        }
        return vo;
    }

    private boolean isProbablyObjectKey(String value) {
        return !value.contains("://");
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
        return ResponseModel.success("资料更新成功", toUserVo(updated));
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

        userMapper.updateAvatar(userId, objectKey);

        User updated = userMapper.selectById(userId);
        return ResponseModel.success("头像更新成功", toUserVo(updated));
    }

    @Override
    public ResponseModel<String> getAvatarUrl(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "用户ID不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseModel.error(ResponseModel.CODE_NOT_FOUND, "用户不存在");
        }

        String avatarValue = user.getUserAvatar();
        if (avatarValue == null || avatarValue.isEmpty()) {
            return ResponseModel.error(ResponseModel.CODE_NOT_FOUND, "该用户未设置头像");
        }

        String avatarUrl;
        if (isProbablyObjectKey(avatarValue)) {
            try {
                avatarUrl = rustFsService.presignedGetUrl(avatarValue, AVATAR_URL_TIMEOUT_MINUTES);
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for avatar key: {}", avatarValue, e);
                return ResponseModel.serverError();
            }
        } else {
            avatarUrl = avatarValue;
        }

        return ResponseModel.success(avatarUrl);
    }
}
