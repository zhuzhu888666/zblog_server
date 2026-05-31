package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistMapper {
    int insert(Playlist playlist);

    Playlist selectById(@Param("playlistId") Long playlistId);

    List<Playlist> selectByUserId(@Param("userId") Long userId);

    int update(Playlist playlist);

    int deleteById(@Param("playlistId") Long playlistId);

    int countByUserId(@Param("userId") Long userId);
}
