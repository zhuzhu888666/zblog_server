package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.service.IFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;

    @PostMapping("/user/favorite/{articleId}")
    public ResponseModel<Boolean> toggleFavorite(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return favoriteService.toggleFavorite(userId, articleId);
    }

    @GetMapping("/user/favorites")
    public ResponseModel<List<ArticleVo>> listUserFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return favoriteService.listUserFavorites(userId);
    }

    @GetMapping("/user/favorite/{articleId}/status")
    public ResponseModel<Boolean> isFavorited(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return favoriteService.isFavorited(userId, articleId);
    }
}
