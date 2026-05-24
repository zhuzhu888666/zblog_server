package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    int insert(Favorite favorite);

    int deleteByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    Favorite selectByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    List<Favorite> selectByUserId(Long userId);

    int countByArticleId(Long articleId);

    int countByUserId(Long userId);
}
