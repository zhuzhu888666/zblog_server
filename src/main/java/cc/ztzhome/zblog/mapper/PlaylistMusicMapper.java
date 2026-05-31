package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.PlaylistMusic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistMusicMapper {
    int insert(PlaylistMusic playlistMusic);

    int deleteByPlaylistAndMusic(@Param("playlistId") Long playlistId, @Param("musicId") Long musicId);

    PlaylistMusic selectByPlaylistAndMusic(@Param("playlistId") Long playlistId, @Param("musicId") Long musicId);

    int deleteByPlaylistId(@Param("playlistId") Long playlistId);

    List<Long> selectMusicIdsByPlaylistId(@Param("playlistId") Long playlistId);

    int countByPlaylistId(@Param("playlistId") Long playlistId);

    Integer selectMaxSortOrder(@Param("playlistId") Long playlistId);
}
