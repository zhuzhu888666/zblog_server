package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Music;
import cc.ztzhome.zblog.bean.entity.MusicFavorite;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;
import cc.ztzhome.zblog.mapper.MusicFavoriteMapper;
import cc.ztzhome.zblog.mapper.MusicMapper;
import cc.ztzhome.zblog.service.IMusicFavoriteService;
import cc.ztzhome.zblog.service.IMusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MusicFavoriteServiceImpl implements IMusicFavoriteService {

    @Autowired
    private MusicFavoriteMapper musicFavoriteMapper;

    @Autowired
    private MusicMapper musicMapper;

    @Autowired
    private IMusicService musicService;

    @Override
    public ResponseModel<Boolean> toggleFavorite(Long userId, Long musicId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (musicId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "音乐ID不能为空");
        }
        Music music = musicMapper.selectById(musicId);
        if (music == null || music.getDeleted() == 1) {
            return ResponseModel.notFound();
        }

        MusicFavorite exists = musicFavoriteMapper.selectByUserAndMusic(userId, musicId);
        if (exists != null) {
            musicFavoriteMapper.deleteByUserAndMusic(userId, musicId);
            return ResponseModel.success("取消收藏", false);
        }

        MusicFavorite fav = new MusicFavorite();
        fav.setUserId(userId);
        fav.setMusicId(musicId);
        musicFavoriteMapper.insert(fav);
        return ResponseModel.success("收藏成功", true);
    }

    @Override
    public ResponseModel<List<MusicVo>> listUserFavorites(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<MusicFavorite> favorites = musicFavoriteMapper.selectByUserId(userId);
        List<MusicVo> voList = new ArrayList<>();
        for (MusicFavorite fav : favorites) {
            Music music = musicMapper.selectById(fav.getMusicId());
            if (music != null && music.getDeleted() == 0) {
                voList.add(musicService.toMusicVo(music));
            }
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<Boolean> isFavorited(Long userId, Long musicId) {
        if (userId == null || musicId == null) {
            return ResponseModel.success(false);
        }
        MusicFavorite exists = musicFavoriteMapper.selectByUserAndMusic(userId, musicId);
        return ResponseModel.success(exists != null);
    }
}
