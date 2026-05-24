package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.Music;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MusicMapper {
    int insertMusic(Music music);

    Music selectById(@Param("musicId") Long musicId);

    List<Music> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    long countAll();

    int updateMusic(Music music);

    int deleteById(@Param("musicId") Long musicId);
}
