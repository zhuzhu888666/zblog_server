package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;

import java.util.List;

public interface IFavoriteService {
    ResponseModel<Boolean> toggleFavorite(Long userId, Long articleId);

    ResponseModel<List<ArticleVo>> listUserFavorites(Long userId);

    ResponseModel<Boolean> isFavorited(Long userId, Long articleId);
}
