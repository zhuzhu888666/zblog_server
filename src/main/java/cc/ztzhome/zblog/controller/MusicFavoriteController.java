package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.service.IMusicFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MusicFavoriteController {

    @Autowired
    private IMusicFavoriteService musicFavoriteService;

    @PostMapping("/music/favorite/{musicId}")
    public ResponseModel<Boolean> toggleFavorite(@PathVariable Long musicId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return musicFavoriteService.toggleFavorite(userId, musicId);
    }

    @GetMapping("/music/favorites")
    public ResponseModel<List<MusicVo>> listUserFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return musicFavoriteService.listUserFavorites(userId);
    }

    @GetMapping("/music/favorite/{musicId}/status")
    public ResponseModel<Boolean> isFavorited(@PathVariable Long musicId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return musicFavoriteService.isFavorited(userId, musicId);
    }
}
