package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;

import java.util.List;

public interface IMusicFavoriteService {
    ResponseModel<Boolean> toggleFavorite(Long userId, Long musicId);

    ResponseModel<List<MusicVo>> listUserFavorites(Long userId);

    ResponseModel<Boolean> isFavorited(Long userId, Long musicId);
}
