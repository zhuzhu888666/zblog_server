package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;

public interface IArticleLikeService {
    ResponseModel<Boolean> toggleLike(Long userId, Long articleId);

    ResponseModel<Boolean> isLiked(Long userId, Long articleId);

    ResponseModel<Integer> getLikeCount(Long articleId);
}
