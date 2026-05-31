package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.BatchStatusDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IUserService;
import cc.ztzhome.zblog.utils.RedisUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final String TOKEN_PREFIX = "token:user:";

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public ResponseModel<PageResult<UserVo>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.listUsersByPage(page, size);
    }

    @PostMapping("/users")
    public ResponseModel<UserVo> createUser(@RequestBody User user) {
        return userService.adminCreateUser(user);
    }

    @PutMapping("/users/{id}")
    public ResponseModel<UserVo> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setUserId(id);
        return userService.adminUpdateUser(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseModel<Void> deleteUser(@PathVariable Long id) {
        return userService.adminDeleteUser(id);
    }

    @PutMapping("/users/batch")
    public ResponseModel<Void> batchUpdateStatus(@Valid @RequestBody BatchStatusDto dto) {
        return userService.adminBatchUpdateStatus(dto.getUserIds(), dto.getStatus());
    }

    @GetMapping("/online-users")
    public ResponseModel<List<UserVo>> getOnlineUsers() {
        Set<String> keys = redisUtil.keys(TOKEN_PREFIX + "*");
        List<UserVo> onlineUsers = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                try {
                    String idStr = key.substring(TOKEN_PREFIX.length());
                    Long userId = Long.parseLong(idStr);
                    User user = userMapper.selectById(userId);
                    if (user != null) {
                        onlineUsers.add(userService.toUserVo(user));
                    }
                } catch (Exception ignored) {
                    // 跳过无效 key
                }
            }
        }
        return ResponseModel.success(onlineUsers);
    }

    @DeleteMapping("/online-users/{userId}")
    public ResponseModel<Void> kickUser(@PathVariable Long userId) {
        redisUtil.delete(TOKEN_PREFIX + userId);
        return ResponseModel.success("已强制下线");
    }
}
