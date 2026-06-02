package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Tag;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.TagVo;
import cc.ztzhome.zblog.mapper.TagMapper;
import cc.ztzhome.zblog.service.ITagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagServiceImpl implements ITagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public ResponseModel<List<TagVo>> listAllTags() {
        List<Tag> tags = tagMapper.selectAll();
        List<TagVo> voList = tags.stream().map(this::toTagVo).collect(Collectors.toList());
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<TagVo> getTag(Long tagId) {
        if (tagId == null) {
            return ResponseModel.error("标签ID不能为空");
        }
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            return ResponseModel.notFound();
        }
        return ResponseModel.success(toTagVo(tag));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseModel<TagVo> createTag(Tag tag) {
        if (tag.getName() == null || tag.getName().isBlank()) {
            return ResponseModel.error("标签名称不能为空");
        }
        if (tag.getName().length() > 32) {
            return ResponseModel.error("标签名称不能超过32个字符");
        }
        tag.setCreateTime(LocalDateTime.now());
        int result = tagMapper.insertTag(tag);
        if (result <= 0) {
            return ResponseModel.serverError();
        }
        return ResponseModel.success("标签创建成功", toTagVo(tag));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseModel<TagVo> updateTag(Long tagId, Tag tag) {
        if (tagId == null) {
            return ResponseModel.error("标签ID不能为空");
        }
        Tag existing = tagMapper.selectById(tagId);
        if (existing == null) {
            return ResponseModel.notFound();
        }
        tag.setTagId(tagId);
        tagMapper.updateTag(tag);
        Tag updated = tagMapper.selectById(tagId);
        return ResponseModel.success("更新成功", toTagVo(updated));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseModel<Void> deleteTag(Long tagId) {
        if (tagId == null) {
            return ResponseModel.error("标签ID不能为空");
        }
        Tag existing = tagMapper.selectById(tagId);
        if (existing == null) {
            return ResponseModel.notFound();
        }
        tagMapper.deleteTag(tagId);
        return ResponseModel.success("删除成功");
    }

    @Override
    public List<TagVo> getTagsByArticleId(Long articleId) {
        if (articleId == null) {
            return new ArrayList<>();
        }
        List<Tag> tags = tagMapper.selectByArticleId(articleId);
        return tags.stream().map(this::toTagVo).collect(Collectors.toList());
    }

    @Override
    public List<Long> matchTags(String title, String content) {
        if (title == null) title = "";
        if (content == null) content = "";

        String combined = (title + " " + content).toLowerCase();
        List<Tag> allTags = tagMapper.selectAll();
        List<Long> matchedIds = new ArrayList<>();

        for (Tag tag : allTags) {
            if (tag.getKeywords() == null || tag.getKeywords().isBlank()) {
                continue;
            }
            String[] keywords = tag.getKeywords().split(",");
            for (String kw : keywords) {
                String trimmed = kw.trim().toLowerCase();
                if (!trimmed.isEmpty() && combined.contains(trimmed)) {
                    matchedIds.add(tag.getTagId());
                    break;
                }
            }
        }
        return matchedIds;
    }

    private TagVo toTagVo(Tag tag) {
        TagVo vo = new TagVo();
        vo.setTagId(tag.getTagId());
        vo.setName(tag.getName());
        vo.setIcon(tag.getIcon());
        vo.setKeywords(tag.getKeywords());
        vo.setArticleCount(tag.getArticleCount());
        vo.setCreateTime(tag.getCreateTime());
        return vo;
    }
}
