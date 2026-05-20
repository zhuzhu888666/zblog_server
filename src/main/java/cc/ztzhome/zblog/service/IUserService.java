package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {

    UserVo toUserVo(User user);

    ResponseModel<UserVo> updateProfile(Long userId, UpdateUserDto dto);

    ResponseModel<UserVo> updateAvatar(Long userId, MultipartFile file);

    ResponseModel<String> getAvatarUrl(Long userId);
}
