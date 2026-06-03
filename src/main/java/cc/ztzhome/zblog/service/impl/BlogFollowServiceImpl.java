package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.BlogFollow;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.BlogFollowMapper;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IBlogFollowService;
import cc.ztzhome.zblog.service.RustFsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BlogFollowServiceImpl implements IBlogFollowService {

    private static final long URL_TIMEOUT_MINUTES = AppConstants.URL_TIMEOUT;

    @Autowired
    private BlogFollowMapper blogFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RustFsService rustFsService;

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

        BlogFollow exists = blogFollowMapper.selectByFollowerAndFollowee(followerId, followeeId);
        if (exists != null) {
            blogFollowMapper.deleteByFollowerAndFollowee(followerId, followeeId);
            return ResponseModel.success("关注已取消", false);
        }

        BlogFollow blogFollow = new BlogFollow();
        blogFollow.setFollowerId(followerId);
        blogFollow.setFolloweeId(followeeId);
        blogFollowMapper.insert(blogFollow);
        return ResponseModel.success("关注成功", true);
    }

    @Override
    public ResponseModel<Boolean> isFollowing(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            return ResponseModel.success(false);
        }
        BlogFollow exists = blogFollowMapper.selectByFollowerAndFollowee(followerId, followeeId);
        return ResponseModel.success(exists != null);
    }

    @Override
    public ResponseModel<List<UserVo>> listFollowing(Long followerId) {
        if (followerId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<BlogFollow> follows = blogFollowMapper.selectFollowingByUserId(followerId);
        List<UserVo> voList = new ArrayList<>();
        for (BlogFollow follow : follows) {
            User user = userMapper.selectById(follow.getFolloweeId());
            if (user != null && user.getStatus() == 0) {
                voList.add(toUserVo(user));
            }
        }
        return ResponseModel.success(voList);
    }

    private UserVo toUserVo(User user) {
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
        if (avatarValue != null && !avatarValue.isEmpty() && !avatarValue.contains("://")) {
            try {
                vo.setUserAvatar(rustFsService.presignedGetUrl(avatarValue, URL_TIMEOUT_MINUTES));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for user avatar", e);
            }
        } else if (avatarValue != null && avatarValue.contains("://")) {
            vo.setUserAvatar(avatarValue);
        }

        return vo;
    }
}
