package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.PlaylistDetailVo;
import cc.ztzhome.zblog.bean.vo.PlaylistVo;
import cc.ztzhome.zblog.service.IPlaylistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PlaylistController {

    @Autowired
    private IPlaylistService playlistService;

    @PostMapping("/music/playlist")
    public ResponseModel<PlaylistVo> createPlaylist(@RequestBody(required = false) Map<String, String> body,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String name = body != null ? body.get("name") : null;
        String description = body != null ? body.get("description") : null;
        return playlistService.createPlaylist(userId, name, description);
    }

    @GetMapping("/music/playlists")
    public ResponseModel<List<PlaylistVo>> listUserPlaylists(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return playlistService.listUserPlaylists(userId);
    }

    @GetMapping("/music/playlist/{id}")
    public ResponseModel<PlaylistDetailVo> getPlaylistDetail(@PathVariable Long id,
                                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return playlistService.getPlaylistDetail(userId, id);
    }

    @PutMapping("/music/playlist/{id}")
    public ResponseModel<PlaylistVo> updatePlaylist(@PathVariable Long id,
                                                     @RequestBody(required = false) Map<String, String> body,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String name = body != null ? body.get("name") : null;
        String description = body != null ? body.get("description") : null;
        return playlistService.updatePlaylist(userId, id, name, description);
    }

    @DeleteMapping("/music/playlist/{id}")
    public ResponseModel<Void> deletePlaylist(@PathVariable Long id,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return playlistService.deletePlaylist(userId, id);
    }

    @PostMapping("/music/playlist/{id}/music/{musicId}")
    public ResponseModel<Void> addMusicToPlaylist(@PathVariable Long id,
                                                   @PathVariable Long musicId,
                                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return playlistService.addMusicToPlaylist(userId, id, musicId);
    }

    @DeleteMapping("/music/playlist/{id}/music/{musicId}")
    public ResponseModel<Void> removeMusicFromPlaylist(@PathVariable Long id,
                                                        @PathVariable Long musicId,
                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return playlistService.removeMusicFromPlaylist(userId, id, musicId);
    }
}
