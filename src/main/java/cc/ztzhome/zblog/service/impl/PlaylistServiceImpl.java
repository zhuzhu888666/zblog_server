package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Music;
import cc.ztzhome.zblog.bean.entity.Playlist;
import cc.ztzhome.zblog.bean.entity.PlaylistMusic;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.bean.vo.PlaylistDetailVo;
import cc.ztzhome.zblog.bean.vo.PlaylistVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.MusicMapper;
import cc.ztzhome.zblog.mapper.PlaylistMapper;
import cc.ztzhome.zblog.mapper.PlaylistMusicMapper;
import cc.ztzhome.zblog.service.IPlaylistService;
import cc.ztzhome.zblog.service.IMusicService;
import cc.ztzhome.zblog.service.RustFsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PlaylistServiceImpl implements IPlaylistService {

    @Autowired
    private PlaylistMapper playlistMapper;

    @Autowired
    private PlaylistMusicMapper playlistMusicMapper;

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private IMusicService musicService;

    @Autowired
    private RustFsService rustFsService;

    @Override
    public ResponseModel<PlaylistVo> createPlaylist(Long userId, String name, String description) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (name == null || name.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "歌单名称不能为空");
        }

        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setName(name.trim());
        playlist.setDescription(description != null ? description.trim() : "");
        playlist.setCoverPath("");
        playlist.setCreateTime(LocalDateTime.now());
        playlist.setUpdateTime(LocalDateTime.now());
        playlistMapper.insert(playlist);

        return ResponseModel.success("创建成功", toPlaylistVo(playlist));
    }

    @Override
    public ResponseModel<List<PlaylistVo>> listUserPlaylists(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }

        List<Playlist> playlists = playlistMapper.selectByUserId(userId);
        List<PlaylistVo> voList = new ArrayList<>();
        for (Playlist playlist : playlists) {
            voList.add(toPlaylistVo(playlist));
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<PlaylistDetailVo> getPlaylistDetail(Long userId, Long playlistId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (playlistId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "歌单ID不能为空");
        }

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return ResponseModel.notFound();
        }
        if (playlist.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权访问该歌单");
        }

        PlaylistDetailVo detail = new PlaylistDetailVo();
        detail.setPlaylistId(playlist.getPlaylistId());
        detail.setName(playlist.getName());
        detail.setDescription(playlist.getDescription());
        detail.setCreateTime(playlist.getCreateTime());
        detail.setUpdateTime(playlist.getUpdateTime());

        if (playlist.getCoverPath() != null && !playlist.getCoverPath().isEmpty()) {
            try {
                detail.setCoverUrl(rustFsService.presignedGetUrl(playlist.getCoverPath(), AppConstants.URL_TIMEOUT));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for playlist cover: {}", playlist.getCoverPath(), e);
            }
        }

        List<Long> musicIds = playlistMusicMapper.selectMusicIdsByPlaylistId(playlistId);
        List<MusicVo> songs = new ArrayList<>();
        for (Long musicId : musicIds) {
            Music music = musicMapper.selectById(musicId);
            if (music != null && music.getDeleted() == 0) {
                songs.add(musicService.toMusicVo(music));
            }
        }
        detail.setSongs(songs);
        detail.setSongCount(songs.size());

        return ResponseModel.success(detail);
    }

    @Override
    public ResponseModel<PlaylistVo> updatePlaylist(Long userId, Long playlistId, String name, String description) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (playlistId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "歌单ID不能为空");
        }

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return ResponseModel.notFound();
        }
        if (playlist.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权修改该歌单");
        }

        if (name != null && !name.isBlank()) {
            playlist.setName(name.trim());
        }
        if (description != null) {
            playlist.setDescription(description.trim());
        }
        playlistMapper.update(playlist);

        Playlist updated = playlistMapper.selectById(playlistId);
        return ResponseModel.success("更新成功", toPlaylistVo(updated));
    }

    @Override
    public ResponseModel<Void> deletePlaylist(Long userId, Long playlistId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (playlistId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "歌单ID不能为空");
        }

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return ResponseModel.notFound();
        }
        if (playlist.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权删除该歌单");
        }

        playlistMapper.deleteById(playlistId);
        playlistMusicMapper.deleteByPlaylistId(playlistId);
        return ResponseModel.success("删除成功");
    }

    @Override
    public ResponseModel<Void> addMusicToPlaylist(Long userId, Long playlistId, Long musicId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (playlistId == null || musicId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "参数不能为空");
        }

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return ResponseModel.notFound();
        }
        if (playlist.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权操作该歌单");
        }

        Music music = musicMapper.selectById(musicId);
        if (music == null || music.getDeleted() == 1) {
            return ResponseModel.notFound();
        }

        PlaylistMusic existing = playlistMusicMapper.selectByPlaylistAndMusic(playlistId, musicId);
        if (existing != null) {
            return ResponseModel.error("歌曲已在歌单中");
        }

        Integer maxSort = playlistMusicMapper.selectMaxSortOrder(playlistId);
        int sortOrder = (maxSort != null ? maxSort : 0) + 1;

        PlaylistMusic pm = new PlaylistMusic();
        pm.setPlaylistId(playlistId);
        pm.setMusicId(musicId);
        pm.setSortOrder(sortOrder);
        pm.setCreateTime(LocalDateTime.now());
        playlistMusicMapper.insert(pm);

        return ResponseModel.success("添加成功");
    }

    @Override
    public ResponseModel<Void> removeMusicFromPlaylist(Long userId, Long playlistId, Long musicId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (playlistId == null || musicId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "参数不能为空");
        }

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null) {
            return ResponseModel.notFound();
        }
        if (playlist.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "无权操作该歌单");
        }

        playlistMusicMapper.deleteByPlaylistAndMusic(playlistId, musicId);
        return ResponseModel.success("移除成功");
    }

    private PlaylistVo toPlaylistVo(Playlist playlist) {
        PlaylistVo vo = new PlaylistVo();
        vo.setPlaylistId(playlist.getPlaylistId());
        vo.setName(playlist.getName());
        vo.setDescription(playlist.getDescription());
        vo.setCreateTime(playlist.getCreateTime());
        vo.setUpdateTime(playlist.getUpdateTime());

        if (playlist.getCoverPath() != null && !playlist.getCoverPath().isEmpty()) {
            try {
                vo.setCoverUrl(rustFsService.presignedGetUrl(playlist.getCoverPath(), AppConstants.URL_TIMEOUT));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for playlist cover: {}", playlist.getCoverPath(), e);
            }
        }

        int songCount = playlistMusicMapper.countByPlaylistId(playlist.getPlaylistId());
        vo.setSongCount(songCount);

        return vo;
    }
}
