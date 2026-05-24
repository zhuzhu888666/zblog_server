package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.UpdateUserDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.UserVo;
import cc.ztzhome.zblog.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/user/profile")
    public ResponseModel<UserVo> updateProfile(@Valid @RequestBody UpdateUserDto dto,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateProfile(userId, dto);
    }

    @PostMapping("/user/avatar")
    public ResponseModel<UserVo> updateAvatar(@RequestParam("file") MultipartFile file,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateAvatar(userId, file);
    }

    @GetMapping("/public/getAvatar/{userId}")
    public ResponseModel<String> getAvatarUrl(@PathVariable Long userId) {
        return userService.getAvatarUrl(userId);
    }
}
