package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.dto.CreateCommentDto;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.CommentVo;
import cc.ztzhome.zblog.service.ICommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @GetMapping("/public/article/{articleId}/comments")
    public ResponseModel<List<CommentVo>> listComments(@PathVariable Long articleId) {
        return commentService.listComments(articleId);
    }

    @GetMapping("/public/article/{articleId}/comment/count")
    public ResponseModel<Integer> getCommentCount(@PathVariable Long articleId) {
        return commentService.getCommentCount(articleId);
    }

    @PostMapping("/user/comment")
    public ResponseModel<CommentVo> createComment(
            @RequestBody CreateCommentDto dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return commentService.createComment(userId, dto.getArticleId(), dto.getParentId(), dto.getReplyToUserId(), dto.getContent());
    }

    @DeleteMapping("/user/comment/{commentId}")
    public ResponseModel<Void> deleteComment(@PathVariable Long commentId,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return commentService.deleteComment(userId, commentId);
    }
}
