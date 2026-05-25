package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.BlogHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogHistoryMapper {
    int insertOrUpdate(BlogHistory blogHistory);

    int deleteByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    int deleteByUserId(@Param("userId") Long userId);

    List<BlogHistory> selectByUserId(@Param("userId") Long userId);
}
