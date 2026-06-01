package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.BatchArticleDto;
import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.service.IArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
            @RequestParam(value = "articleType", required = false) String articleType,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleService.createArticle(userId, title, content, articleType, cover);
    }

    @GetMapping("/public/article/list")
    public ResponseModel<List<ArticleVo>> listArticles() {
        return articleService.listArticles();
    }

    @GetMapping("/public/article/random")
    public ResponseModel<List<ArticleVo>> getRandomArticles(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return articleService.getRandomArticles(page, pageSize);
    }

    @GetMapping("/user/articles")
    public ResponseModel<List<ArticleVo>> listUserArticles(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleService.listUserArticles(userId);
    }

    @DeleteMapping("/user/article/{articleId}")
    public ResponseModel<Void> deleteArticle(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleService.deleteArticle(userId, articleId);
    }

    @GetMapping("/public/article/{articleId}")
    public ResponseModel<ArticleVo> getArticle(@PathVariable Long articleId) {
        return articleService.getArticle(articleId);
    }

    @GetMapping("/admin/articles")
    public ResponseModel<PageResult<ArticleVo>> listArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return articleService.listArticles(page, size, keyword, status);
    }

    @GetMapping("/admin/articles/{id}")
    public ResponseModel<ArticleVo> getArticleAdmin(@PathVariable Long id) {
        return articleService.getArticle(id);
    }

    @PutMapping("/admin/articles/{id}")
    public ResponseModel<ArticleVo> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        return articleService.adminUpdateArticle(id, article);
    }

    @DeleteMapping("/admin/articles/{id}")
    public ResponseModel<Void> deleteArticleAdmin(@PathVariable Long id) {
        return articleService.adminDeleteArticle(id);
    }

    @PutMapping("/admin/articles/batch")
    public ResponseModel<Void> batchUpdateArticles(@Valid @RequestBody BatchArticleDto dto) {
        return articleService.adminBatchUpdateArticles(dto);
    }
}
