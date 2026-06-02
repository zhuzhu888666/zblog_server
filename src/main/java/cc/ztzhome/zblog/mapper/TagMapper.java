package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    List<Tag> selectAll();

    Tag selectById(@Param("tagId") Long tagId);

    List<Tag> selectByArticleId(@Param("articleId") Long articleId);

    int insertTag(Tag tag);

    int updateTag(Tag tag);

    int deleteTag(@Param("tagId") Long tagId);

    int insertArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    int deleteArticleTags(@Param("articleId") Long articleId);

    List<Tag> selectByArticleIds(@Param("articleIds") List<Long> articleIds);
}
