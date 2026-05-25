package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;

import java.util.List;

public interface IBlogHistoryService {
    ResponseModel<Void> recordView(Long userId, Long articleId);

    ResponseModel<List<ArticleVo>> listUserHistory(Long userId);

    ResponseModel<Void> deleteHistory(Long userId, Long articleId);

    ResponseModel<Void> clearHistory(Long userId);
}
