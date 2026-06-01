package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.service.IArticleLikeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ArticleLikeController {

    @Autowired
    private IArticleLikeService articleLikeService;

    @PostMapping("/user/article/{articleId}/like")
    public ResponseModel<Boolean> toggleLike(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleLikeService.toggleLike(userId, articleId);
    }

    @GetMapping("/user/article/{articleId}/like/status")
    public ResponseModel<Boolean> isLiked(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleLikeService.isLiked(userId, articleId);
    }

    @GetMapping("/public/article/{articleId}/like/count")
    public ResponseModel<Integer> getLikeCount(@PathVariable Long articleId) {
        return articleLikeService.getLikeCount(articleId);
    }
}
