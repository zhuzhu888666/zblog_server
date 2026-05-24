package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.service.IArticleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ArticleController {

    @Autowired
    private IArticleService articleService;

    @PostMapping("/article/create")
    public ResponseModel<ArticleVo> createArticle(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleService.createArticle(userId, title, content, cover);
    }

    @GetMapping("/public/article/list")
    public ResponseModel<List<ArticleVo>> listArticles() {
        return articleService.listArticles();
    }

    @GetMapping("/public/article/{articleId}")
    public ResponseModel<ArticleVo> getArticle(@PathVariable Long articleId) {
        return articleService.getArticle(articleId);
    }
}
