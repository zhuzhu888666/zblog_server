package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.BatchStatusDto;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;

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
}
