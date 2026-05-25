package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);

    int deleteById(@Param("commentId") Long commentId);

    Comment selectById(@Param("commentId") Long commentId);

    List<Comment> selectByArticleId(@Param("articleId") Long articleId);

    int countByArticleId(@Param("articleId") Long articleId);
}
