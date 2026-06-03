package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.service.IFollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class FollowController {

    @Autowired
    private IFollowService followService;

    @PostMapping("/user/follow/{followeeId}")
    public ResponseModel<Boolean> toggleFollow(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return followService.toggleFollow(userId, followeeId);
    }

    @GetMapping("/user/follow/{followeeId}/status")
    public ResponseModel<Boolean> isFollowing(@PathVariable Long followeeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return followService.isFollowing(userId, followeeId);
    }
}
