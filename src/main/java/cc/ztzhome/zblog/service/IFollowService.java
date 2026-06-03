package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;

public interface IFollowService {
    ResponseModel<Boolean> toggleFollow(Long followerId, Long followeeId);

    ResponseModel<Boolean> isFollowing(Long followerId, Long followeeId);
}
