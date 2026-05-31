package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.service.IMusicHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class MusicHistoryController {

    @Autowired
    private IMusicHistoryService musicHistoryService;

    @PostMapping("/music/history/{musicId}")
    public ResponseModel<Void> recordPlay(@PathVariable Long musicId,
                                          @RequestBody(required = false) Map<String, String> body,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDateTime playTime = null;
        if (body != null && body.containsKey("playTime")) {
            try {
                playTime = LocalDateTime.parse(body.get("playTime"));
            } catch (Exception ignored) {
            }
        }
        return musicHistoryService.recordPlay(userId, musicId, playTime);
    }

    @GetMapping("/music/history")
    public ResponseModel<List<MusicVo>> listUserHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return musicHistoryService.listUserHistory(userId);
    }

    @DeleteMapping("/music/history/{musicId}")
    public ResponseModel<Void> deleteHistory(@PathVariable Long musicId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return musicHistoryService.deleteHistory(userId, musicId);
    }

    @DeleteMapping("/music/history")
    public ResponseModel<Void> clearHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return musicHistoryService.clearHistory(userId);
    }
}
