package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.entity.Music;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.bean.vo.PageResult;
import org.springframework.web.multipart.MultipartFile;

public interface IMusicService {
    MusicVo toMusicVo(Music music);

    ResponseModel<MusicVo> uploadMusic(MultipartFile file, MultipartFile cover, MultipartFile lyric,
                                       String title, String artist, String genre,
                                       String duration, String releaseTime);

    ResponseModel<PageResult<MusicVo>> listMusic(int page, int size);

    ResponseModel<MusicVo> updateMusic(Music music);

    ResponseModel<Void> deleteMusic(Long musicId);

    ResponseModel<MusicVo> getMusicById(Long musicId);

    ResponseModel<MusicVo> uploadLyric(Long musicId, MultipartFile lyric);

    ResponseModel<String> getLyricContent(Long musicId);

    ResponseModel<PageResult<MusicVo>> getFavoriteRanking(int page, int size);
}
