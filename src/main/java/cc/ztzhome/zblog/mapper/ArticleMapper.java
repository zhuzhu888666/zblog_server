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

    List<Article> selectByUserId(Long userId);

    int updateCoverKey(@Param("articleId") Long articleId, @Param("coverKey") String coverKey);

    int updateStatus(@Param("articleId") Long articleId, @Param("status") int status);

    List<Article> selectByPageWithFilter(@Param("offset") int offset, @Param("limit") int limit,
                                         @Param("keyword") String keyword, @Param("status") Integer status,
                                         @Param("tagId") Long tagId);

    long countWithFilter(@Param("keyword") String keyword, @Param("status") Integer status,
                         @Param("tagId") Long tagId);

    int updateArticle(Article article);

    List<Article> selectByTagId(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("limit") int limit);

    long countByTagId(@Param("tagId") Long tagId);
}
