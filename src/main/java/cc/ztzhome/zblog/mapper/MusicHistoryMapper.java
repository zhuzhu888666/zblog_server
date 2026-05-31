package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.MusicHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MusicHistoryMapper {
    int insertOrUpdate(MusicHistory history);

    int deleteByUserAndMusic(@Param("userId") Long userId, @Param("musicId") Long musicId);

    int deleteByUserId(@Param("userId") Long userId);

    List<MusicHistory> selectByUserId(@Param("userId") Long userId);
}
