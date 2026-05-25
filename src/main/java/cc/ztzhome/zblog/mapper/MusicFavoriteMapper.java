package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.MusicFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MusicFavoriteMapper {
    int insert(MusicFavorite favorite);

    int deleteByUserAndMusic(@Param("userId") Long userId, @Param("musicId") Long musicId);

    MusicFavorite selectByUserAndMusic(@Param("userId") Long userId, @Param("musicId") Long musicId);

    List<MusicFavorite> selectByUserId(Long userId);

    int countByMusicId(Long musicId);

    int countByUserId(Long userId);
}
