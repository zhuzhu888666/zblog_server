package cc.ztzhome.zblog.controller;

import cc.ztzhome.zblog.bean.entity.Tag;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.TagVo;
import cc.ztzhome.zblog.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TagController {

    @Autowired
    private ITagService tagService;

    @GetMapping("/public/tags")
    public ResponseModel<List<TagVo>> listTags() {
        return tagService.listAllTags();
    }

    @GetMapping("/public/tags/{tagId}")
    public ResponseModel<TagVo> getTag(@PathVariable Long tagId) {
        return tagService.getTag(tagId);
    }

    @PostMapping("/public/tags/match")
    public ResponseModel<List<TagVo>> matchTags(@RequestParam String title, @RequestParam String content) {
        List<Long> matchedIds = tagService.matchTags(title, content);
        List<TagVo> vos = matchedIds.stream()
                .map(id -> tagService.getTag(id).getData())
                .toList();
        return ResponseModel.success(vos);
    }

    @PostMapping("/admin/tags")
    public ResponseModel<TagVo> createTag(@RequestBody Tag tag) {
        return tagService.createTag(tag);
    }

    @PutMapping("/admin/tags/{tagId}")
    public ResponseModel<TagVo> updateTag(@PathVariable Long tagId, @RequestBody Tag tag) {
        return tagService.updateTag(tagId, tag);
    }

    @DeleteMapping("/admin/tags/{tagId}")
    public ResponseModel<Void> deleteTag(@PathVariable Long tagId) {
        return tagService.deleteTag(tagId);
    }
}
