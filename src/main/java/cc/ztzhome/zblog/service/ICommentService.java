package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.CommentVo;

import java.util.List;

public interface ICommentService {
    ResponseModel<List<CommentVo>> listComments(Long articleId);

    ResponseModel<Integer> getCommentCount(Long articleId);

    ResponseModel<CommentVo> createComment(Long userId, Long articleId, Long parentId, Long replyToUserId, String content);

    ResponseModel<Void> deleteComment(Long userId, Long commentId);
}
