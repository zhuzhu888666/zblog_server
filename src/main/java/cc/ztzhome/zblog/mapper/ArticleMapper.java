package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    int insertArticle(Article article);

    Article selectById(Long articleId);

    List<Article> selectPublishedList();

    List<Article> selectRandomPublishedList(@Param("offset") int offset, @Param("limit") int limit);

    int updateCoverKey(@Param("articleId") Long articleId, @Param("coverKey") String coverKey);
}
