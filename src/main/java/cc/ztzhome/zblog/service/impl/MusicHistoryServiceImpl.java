package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Music;
import cc.ztzhome.zblog.bean.entity.MusicHistory;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.mapper.MusicHistoryMapper;
import cc.ztzhome.zblog.mapper.MusicMapper;
import cc.ztzhome.zblog.service.IMusicHistoryService;
import cc.ztzhome.zblog.service.IMusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MusicHistoryServiceImpl implements IMusicHistoryService {

    @Autowired
    private MusicHistoryMapper musicHistoryMapper;

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private IMusicService musicService;

    @Override
    public ResponseModel<Void> recordPlay(Long userId, Long musicId, LocalDateTime playTime) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (musicId == null) {
            return ResponseModel.error("音乐ID不能为空");
        }
        Music music = musicMapper.selectById(musicId);
        if (music == null) {
            return ResponseModel.notFound();
        }

        MusicHistory history = new MusicHistory();
        history.setUserId(userId);
        history.setMusicId(musicId);
        history.setPlayTime(playTime != null ? playTime : LocalDateTime.now());
        musicHistoryMapper.insertOrUpdate(history);
        return ResponseModel.success("记录成功");
    }

    @Override
    public ResponseModel<List<MusicVo>> listUserHistory(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<MusicHistory> histories = musicHistoryMapper.selectByUserId(userId);
        List<MusicVo> voList = new ArrayList<>();
        for (MusicHistory h : histories) {
            Music music = musicMapper.selectById(h.getMusicId());
            if (music != null) {
                voList.add(musicService.toMusicVo(music));
            }
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<Void> deleteHistory(Long userId, Long musicId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (musicId == null) {
            return ResponseModel.error("音乐ID不能为空");
        }
        musicHistoryMapper.deleteByUserAndMusic(userId, musicId);
        return ResponseModel.success("删除成功");
    }

    @Override
    public ResponseModel<Void> clearHistory(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        musicHistoryMapper.deleteByUserId(userId);
        return ResponseModel.success("清除成功");
    }
}
