package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.entity.Music;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.service.IMusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MusicController {

    @Autowired
    private IMusicService musicService;

    @PostMapping("/admin/music/upload")
    public ResponseModel<MusicVo> uploadMusic(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam("title") String title,
            @RequestParam(value = "artist", defaultValue = "") String artist,
            @RequestParam(value = "genre", defaultValue = "") String genre,
            @RequestParam(value = "duration", defaultValue = "00:00") String duration,
            @RequestParam(value = "releaseTime", defaultValue = "") String releaseTime) {
        return musicService.uploadMusic(file, cover, title, artist, genre, duration,
                releaseTime.isEmpty() ? null : releaseTime);
    }

    @GetMapping("/admin/music/list")
    public ResponseModel<PageResult<MusicVo>> listMusic(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return musicService.listMusic(page, size);
    }

    @PutMapping("/admin/music/{id}")
    public ResponseModel<MusicVo> updateMusic(@PathVariable Long id, @RequestBody Music music) {
        music.setMusicId(id);
        return musicService.updateMusic(music);
    }

    @DeleteMapping("/admin/music/{id}")
    public ResponseModel<Void> deleteMusic(@PathVariable Long id) {
        return musicService.deleteMusic(id);
    }

    @GetMapping("/admin/music/{id}")
    public ResponseModel<MusicVo> getMusic(@PathVariable Long id) {
        return musicService.getMusicById(id);
    }

    @GetMapping("/public/music/list")
    public ResponseModel<PageResult<MusicVo>> listPublicMusic(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return musicService.listMusic(page, size);
    }

    @GetMapping("/music/song/url")
    public ResponseModel<MusicVo> getSongUrl(@RequestParam Long songId) {
        return musicService.getMusicById(songId);
    }
}
