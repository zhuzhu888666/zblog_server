package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.BlogFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BlogFollowMapper {
    int insert(BlogFollow blogFollow);

    int deleteByFollowerAndFollowee(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    BlogFollow selectByFollowerAndFollowee(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    List<BlogFollow> selectFollowersByUserId(Long userId);

    List<BlogFollow> selectFollowingByUserId(Long userId);
}
