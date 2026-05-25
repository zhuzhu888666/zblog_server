package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.entity.Comment;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.CommentVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.ArticleMapper;
import cc.ztzhome.zblog.mapper.CommentMapper;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.ICommentService;
import cc.ztzhome.zblog.service.RustFsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CommentServiceImpl implements ICommentService {

    private static final long URL_TIMEOUT_MINUTES = AppConstants.URL_TIMEOUT;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RustFsService rustFsService;

    @Override
    public ResponseModel<List<CommentVo>> listComments(Long articleId) {
        if (articleId == null) {
            return ResponseModel.error("文章ID不能为空");
        }
        List<Comment> comments = commentMapper.selectByArticleId(articleId);
        List<CommentVo> voList = new ArrayList<>();
        for (Comment comment : comments) {
            voList.add(toCommentVo(comment));
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<Integer> getCommentCount(Long articleId) {
        if (articleId == null) {
            return ResponseModel.success(0);
        }
        int count = commentMapper.countByArticleId(articleId);
        return ResponseModel.success(count);
    }

    @Override
    public ResponseModel<CommentVo> createComment(Long userId, Long articleId, Long parentId, Long replyToUserId, String content) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (articleId == null) {
            return ResponseModel.error("文章ID不能为空");
        }
        if (content == null || content.isBlank()) {
            return ResponseModel.error("评论内容不能为空");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return ResponseModel.notFound();
        }

        // 如果是回复，验证父评论存在
        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null) {
                return ResponseModel.error("被回复的评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setReplyToUserId(replyToUserId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);

        return ResponseModel.success("评论成功", toCommentVo(comment));
    }

    @Override
    public ResponseModel<Void> deleteComment(Long userId, Long commentId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (commentId == null) {
            return ResponseModel.error("评论ID不能为空");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return ResponseModel.notFound();
        }
        if (comment.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权删除他人评论");
        }
        commentMapper.deleteById(commentId);
        return ResponseModel.success("删除成功");
    }

    private CommentVo toCommentVo(Comment comment) {
        CommentVo vo = new CommentVo();
        vo.setCommentId(comment.getCommentId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());

        // 被回复的用户名
        if (comment.getReplyToUserId() != null) {
            User replyTo = userMapper.selectById(comment.getReplyToUserId());
            if (replyTo != null) {
                vo.setReplyToUserName(replyTo.getNickname() != null ? replyTo.getNickname() : replyTo.getEmail());
            }
        }

        // 作者信息
        User author = userMapper.selectById(comment.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getEmail());
            String avatarValue = author.getUserAvatar();
            if (avatarValue != null && !avatarValue.isEmpty() && !avatarValue.contains("://")) {
                try {
                    vo.setAuthorAvatar(rustFsService.presignedGetUrl(avatarValue, URL_TIMEOUT_MINUTES));
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for author avatar", e);
                }
            } else if (avatarValue != null && avatarValue.contains("://")) {
                vo.setAuthorAvatar(avatarValue);
            }
        }

        return vo;
    }
}
