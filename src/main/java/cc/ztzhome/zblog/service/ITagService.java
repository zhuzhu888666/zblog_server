package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.entity.Tag;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.TagVo;

import java.util.List;

public interface ITagService {

    ResponseModel<List<TagVo>> listAllTags();

    ResponseModel<TagVo> getTag(Long tagId);

    ResponseModel<TagVo> createTag(Tag tag);

    ResponseModel<TagVo> updateTag(Long tagId, Tag tag);

    ResponseModel<Void> deleteTag(Long tagId);

    List<TagVo> getTagsByArticleId(Long articleId);

    List<Long> matchTags(String title, String content);
}
