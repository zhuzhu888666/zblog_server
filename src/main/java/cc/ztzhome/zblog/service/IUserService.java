package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.bean.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {

    UserVo toUserVo(User user);

    ResponseModel<UserVo> updateProfile(Long userId, UpdateUserDto dto);

    ResponseModel<UserVo> updateAvatar(Long userId, MultipartFile file);

    ResponseModel<String> getAvatarUrl(Long userId);

    ResponseModel<PageResult<UserVo>> listUsersByPage(int page, int size);

    ResponseModel<UserVo> adminCreateUser(User user);

    ResponseModel<UserVo> adminUpdateUser(User user);

    ResponseModel<Void> adminDeleteUser(Long userId);

    ResponseModel<Void> adminBatchUpdateStatus(List<Long> userIds, Integer status);
}
