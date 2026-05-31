package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.PlaylistDetailVo;
import cc.ztzhome.zblog.bean.vo.PlaylistVo;

import java.util.List;

public interface IPlaylistService {
    ResponseModel<PlaylistVo> createPlaylist(Long userId, String name, String description);

    ResponseModel<List<PlaylistVo>> listUserPlaylists(Long userId);

    ResponseModel<PlaylistDetailVo> getPlaylistDetail(Long userId, Long playlistId);

    ResponseModel<PlaylistVo> updatePlaylist(Long userId, Long playlistId, String name, String description);

    ResponseModel<Void> deletePlaylist(Long userId, Long playlistId);

    ResponseModel<Void> addMusicToPlaylist(Long userId, Long playlistId, Long musicId);

    ResponseModel<Void> removeMusicFromPlaylist(Long userId, Long playlistId, Long musicId);
}
