package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {
    int insert(Follow follow);

    int deleteByFollowerAndFollowee(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    Follow selectByFollowerAndFollowee(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);

    List<Follow> selectFollowersByUserId(Long userId);

    List<Follow> selectFollowingByUserId(Long userId);
}
