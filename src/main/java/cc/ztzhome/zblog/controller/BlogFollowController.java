package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.service.IBlogFollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BlogFollowController {

    @Autowired
    private IBlogFollowService blogFollowService;

    @PostMapping("/user/follow/{followeeId}")
    public ResponseModel<Boolean> toggleFollow(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogFollowService.toggleFollow(userId, followeeId);
    }

    @GetMapping("/user/follow/{followeeId}/status")
    public ResponseModel<Boolean> isFollowing(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogFollowService.isFollowing(userId, followeeId);
    }

    @GetMapping("/user/following")
    public ResponseModel<List<UserVo>> listFollowing(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogFollowService.listFollowing(userId);
    }
}
