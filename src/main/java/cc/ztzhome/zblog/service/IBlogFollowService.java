package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.UserVo;

import java.util.List;

public interface IBlogFollowService {
    ResponseModel<Boolean> toggleFollow(Long followerId, Long followeeId);

    ResponseModel<Boolean> isFollowing(Long followerId, Long followeeId);

    ResponseModel<List<UserVo>> listFollowing(Long followerId);
}
