package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Music;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.MusicMapper;
import cc.ztzhome.zblog.service.IMusicService;
import cc.ztzhome.zblog.service.RustFsService;
import cc.ztzhome.zblog.utils.FileTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MusicServiceImpl implements IMusicService {

    private static final long MAX_MUSIC_SIZE = 50 * 1024 * 1024;
    private static final long MAX_COVER_SIZE = 10 * 1024 * 1024;
    private static final long MAX_LYRIC_SIZE = 512 * 1024;

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private RustFsService rustFsService;

    @Override
    public MusicVo toMusicVo(Music music) {
        if (music == null) return null;
        MusicVo vo = new MusicVo();
        vo.setMusicId(music.getMusicId());
        vo.setTitle(music.getTitle());
        vo.setArtistId(music.getArtistId());
        vo.setArtist(music.getArtist());
        vo.setDuration(music.getDuration());
        vo.setGenre(music.getGenre());
        vo.setReleaseTime(music.getReleaseTime());
        vo.setCreateTime(music.getCreateTime());
        vo.setUpdateTime(music.getUpdateTime());
        vo.setFavoriteCount(music.getFavoriteCount());

        if (music.getFilePath() != null && !music.getFilePath().isEmpty()) {
            try {
                vo.setAudioUrl(rustFsService.presignedGetUrl(music.getFilePath(), AppConstants.URL_TIMEOUT));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for music key: {}", music.getFilePath(), e);
            }
        }
        if (music.getCoverPath() != null && !music.getCoverPath().isEmpty()) {
            try {
                vo.setCoverUrl(rustFsService.presignedGetUrl(music.getCoverPath(), AppConstants.URL_TIMEOUT));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for cover key: {}", music.getCoverPath(), e);
            }
        }
        return vo;
    }

    @Override
    public ResponseModel<MusicVo> uploadMusic(MultipartFile file, MultipartFile cover, MultipartFile lyric,
                                              String title, String artist, String genre,
                                              String duration, String releaseTime) {
        if (file == null || file.isEmpty()) {
            return ResponseModel.error("请选择要上传的音乐文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (!"music".equals(FileTypeUtil.getFileType(originalFilename))) {
            return ResponseModel.error("不支持的音频格式，仅支持 MP3、WAV、FLAC、AAC、OGG、M4A、WMA");
        }

        if (file.getSize() > MAX_MUSIC_SIZE) {
            return ResponseModel.error("音乐文件大小不能超过 50MB");
        }

        if (title == null || title.isBlank()) {
            return ResponseModel.error("歌曲标题不能为空");
        }

        String durationStr = (duration != null && !duration.isBlank()) ? duration : "00:00";





        Music music = new Music();
        music.setTitle(title);
        music.setArtist(artist != null ? artist : "");
        music.setGenre(genre != null ? genre : "");
        music.setDuration(durationStr);
        music.setFilePath("");
        music.setCoverPath("");
        musicMapper.insertMusic(music);

        String extension = FileTypeUtil.getFileExtension2(originalFilename);
        String filePath = AppConstants.MUSIC_SAVE_PATH + music.getMusicId()+title + extension;

        try {
            rustFsService.upload(filePath, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            log.error("Failed to upload music file to RustFS", e);
            return ResponseModel.serverError();
        }

        music.setFilePath(filePath);

        if (cover != null && !cover.isEmpty()) {
            String coverFilename = cover.getOriginalFilename();
            if (!"image".equals(FileTypeUtil.getFileType(coverFilename))) {
                return ResponseModel.error("封面文件格式不支持，仅支持常见图片格式");
            }
            if (cover.getSize() > MAX_COVER_SIZE) {
                return ResponseModel.error("封面图片大小不能超过 10MB");
            }
            String coverExtension = FileTypeUtil.getFileExtension2(coverFilename);
            String coverPath = AppConstants.MUSIC_COVER_PATH + music.getMusicId() +title+ coverExtension;
            try {
                rustFsService.upload(coverPath, cover.getInputStream(), cover.getSize(), cover.getContentType());
                music.setCoverPath(coverPath);
            } catch (IOException e) {
                log.error("Failed to upload cover to RustFS", e);
            }
        }

        if (lyric != null && !lyric.isEmpty()) {
            String lyricFilename = lyric.getOriginalFilename();
            String lyricExt = FileTypeUtil.getFileExtension(lyricFilename).toLowerCase();
            if (!"lrc".equals(lyricExt)) {
                return ResponseModel.error("歌词文件格式不支持，仅支持 LRC 格式");
            }
            if (lyric.getSize() > MAX_LYRIC_SIZE) {
                return ResponseModel.error("歌词文件大小不能超过 512KB");
            }
            String lyricPath = AppConstants.MUSIC_LYRIC_PATH + music.getMusicId() + title + ".lrc";
            try {
                rustFsService.upload(lyricPath, lyric.getInputStream(), lyric.getSize(), lyric.getContentType());
                music.setLyricPath(lyricPath);
            } catch (IOException e) {
                log.error("Failed to upload lyric to RustFS", e);
            }
        }

        musicMapper.updateMusic(music);

        Music saved = musicMapper.selectById(music.getMusicId());
        return ResponseModel.success("上传成功", toMusicVo(saved));
    }

    @Override
    public ResponseModel<PageResult<MusicVo>> listMusic(int page, int size) {
        int offset = (page - 1) * size;
        List<Music> records = musicMapper.selectByPage(offset, size);
        long total = musicMapper.countAll();
        List<MusicVo> vos = records.stream().map(this::toMusicVo).toList();
        return ResponseModel.success(new PageResult<>(vos, total, page, size));
    }

    @Override
    public ResponseModel<MusicVo> updateMusic(Music music) {
        if (music.getMusicId() == 0) {
            return ResponseModel.error("音乐ID不能为空");
        }
        Music existing = musicMapper.selectById(music.getMusicId());
        if (existing == null) {
            return ResponseModel.notFound();
        }
        musicMapper.updateMusic(music);
        Music updated = musicMapper.selectById(music.getMusicId());
        return ResponseModel.success("更新成功", toMusicVo(updated));
    }

    @Override
    public ResponseModel<Void> deleteMusic(Long musicId) {
        if (musicId == null) {
            return ResponseModel.error("音乐ID不能为空");
        }
        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return ResponseModel.notFound();
        }

        if (music.getFilePath() != null && !music.getFilePath().isEmpty()) {
            try {
                rustFsService.deleteObject(music.getFilePath());
            } catch (Exception e) {
                log.warn("Failed to delete music object from RustFS: {}", music.getFilePath(), e);
            }
        }
        if (music.getCoverPath() != null && !music.getCoverPath().isEmpty()) {
            try {
                rustFsService.deleteObject(music.getCoverPath());
            } catch (Exception e) {
                log.warn("Failed to delete cover object from RustFS: {}", music.getCoverPath(), e);
            }
        }
        if (music.getLyricPath() != null && !music.getLyricPath().isEmpty()) {
            try {
                rustFsService.deleteObject(music.getLyricPath());
            } catch (Exception e) {
                log.warn("Failed to delete lyric object from RustFS: {}", music.getLyricPath(), e);
            }
        }

        musicMapper.deleteById(musicId);
        return ResponseModel.success("删除成功");
    }

    @Override
    public ResponseModel<MusicVo> getMusicById(Long musicId) {
        if (musicId == null) {
            return ResponseModel.error("音乐ID不能为空");
        }
        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return ResponseModel.notFound();
        }
        return ResponseModel.success(toMusicVo(music));
    }

    @Override
    public ResponseModel<MusicVo> uploadLyric(Long musicId, MultipartFile lyric) {
        if (musicId == null) {
            return ResponseModel.error("音乐ID不能为空");
        }
        if (lyric == null || lyric.isEmpty()) {
            return ResponseModel.error("请选择歌词文件");
        }

        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return ResponseModel.notFound();
        }

        String lyricFilename = lyric.getOriginalFilename();
        String lyricExt = FileTypeUtil.getFileExtension(lyricFilename).toLowerCase();
        if (!"lrc".equals(lyricExt)) {
            return ResponseModel.error("歌词文件格式不支持，仅支持 LRC 格式");
        }
        if (lyric.getSize() > MAX_LYRIC_SIZE) {
            return ResponseModel.error("歌词文件大小不能超过 512KB");
        }

        // Delete old lyric file if exists
        if (music.getLyricPath() != null && !music.getLyricPath().isEmpty()) {
            try {
                rustFsService.deleteObject(music.getLyricPath());
            } catch (Exception e) {
                log.warn("Failed to delete old lyric from RustFS: {}", music.getLyricPath(), e);
            }
        }

        String lyricPath = AppConstants.MUSIC_LYRIC_PATH + music.getMusicId() + music.getTitle() + ".lrc";
        try {
            rustFsService.upload(lyricPath, lyric.getInputStream(), lyric.getSize(), lyric.getContentType());
        } catch (IOException e) {
            log.error("Failed to upload lyric to RustFS", e);
            return ResponseModel.serverError();
        }

        music.setLyricPath(lyricPath);
        musicMapper.updateMusic(music);

        Music updated = musicMapper.selectById(musicId);
        return ResponseModel.success("歌词上传成功", toMusicVo(updated));
    }

    @Override
    public ResponseModel<PageResult<MusicVo>> getFavoriteRanking(int page, int size) {
        int offset = (page - 1) * size;
        List<Music> records = musicMapper.selectFavoriteRanking(offset, size);
        long total = musicMapper.countFavoriteRanking();
        List<MusicVo> vos = records.stream().map(this::toMusicVo).toList();
        return ResponseModel.success(new PageResult<>(vos, total, page, size));
    }

    @Override
    public ResponseModel<String> getLyricContent(Long musicId) {
        if (musicId == null) {
            return ResponseModel.error("音乐ID不能为空");
        }
        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return ResponseModel.notFound();
        }
        if (music.getLyricPath() == null || music.getLyricPath().isEmpty()) {
            return ResponseModel.error("该歌曲暂无歌词");
        }
        try {
            byte[] bytes = rustFsService.downloadAsBytes(music.getLyricPath());
            String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            return ResponseModel.success("操作成功", content);
        } catch (Exception e) {
            log.error("Failed to download lyric from RustFS: {}", music.getLyricPath(), e);
            return ResponseModel.serverError();
        }
    }
}
