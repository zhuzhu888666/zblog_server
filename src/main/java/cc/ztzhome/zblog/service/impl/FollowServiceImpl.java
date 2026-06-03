package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Follow;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.mapper.FollowMapper;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowServiceImpl implements IFollowService {

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ResponseModel<Boolean> toggleFollow(Long followerId, Long followeeId) {
        if (followerId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (followeeId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "用户ID不能为空");
        }
        if (followerId.equals(followeeId)) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "不能关注自己");
        }
        User followee = userMapper.selectById(followeeId);
        if (followee == null) {
            return ResponseModel.notFound();
        }

        Follow exists = followMapper.selectByFollowerAndFollowee(followerId, followeeId);
        if (exists != null) {
            followMapper.deleteByFollowerAndFollowee(followerId, followeeId);
            return ResponseModel.success("关注已取消", false);
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followMapper.insert(follow);
        return ResponseModel.success("关注成功", true);
    }

    @Override
    public ResponseModel<Boolean> isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return ResponseModel.success(false);
        }
        Follow exists = followMapper.selectByFollowerAndFollowee(followerId, followeeId);
        return ResponseModel.success(exists != null);
    }
}
