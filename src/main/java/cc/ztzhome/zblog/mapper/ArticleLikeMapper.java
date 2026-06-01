package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleLikeMapper {
    int insert(ArticleLike articleLike);

    int deleteByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    ArticleLike selectByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    int countByArticleId(Long articleId);
}
