package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.service.IBlogHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BlogHistoryController {

    @Autowired
    private IBlogHistoryService blogHistoryService;

    @PostMapping("/user/history/{articleId}")
    public ResponseModel<Void> recordView(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogHistoryService.recordView(userId, articleId);
    }

    @GetMapping("/user/history")
    public ResponseModel<List<ArticleVo>> listUserHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogHistoryService.listUserHistory(userId);
    }

    @DeleteMapping("/user/history/{articleId}")
    public ResponseModel<Void> deleteHistory(@PathVariable Long articleId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogHistoryService.deleteHistory(userId, articleId);
    }

    @DeleteMapping("/user/history")
    public ResponseModel<Void> clearHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return blogHistoryService.clearHistory(userId);
    }
}
