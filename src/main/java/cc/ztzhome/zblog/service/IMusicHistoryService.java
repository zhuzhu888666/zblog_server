package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.MusicVo;

import java.time.LocalDateTime;
import java.util.List;

public interface IMusicHistoryService {
    ResponseModel<Void> recordPlay(Long userId, Long musicId, LocalDateTime playTime);

    ResponseModel<List<MusicVo>> listUserHistory(Long userId);

    ResponseModel<Void> deleteHistory(Long userId, Long musicId);

    ResponseModel<Void> clearHistory(Long userId);
}
