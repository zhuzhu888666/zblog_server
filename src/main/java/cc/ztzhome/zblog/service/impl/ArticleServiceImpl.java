package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.dto.BatchArticleDto;
import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.bean.vo.PageResult;
import cc.ztzhome.zblog.bean.vo.TagVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.ArticleMapper;
import cc.ztzhome.zblog.mapper.TagMapper;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IArticleService;
import cc.ztzhome.zblog.service.ITagService;
import cc.ztzhome.zblog.service.RustFsService;
import cc.ztzhome.zblog.utils.FileTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl implements IArticleService {

    private static final long ARTICLE_URL_TIMEOUT_MINUTES = AppConstants.URL_TIMEOUT;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ITagService tagService;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private RustFsService rustFsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseModel<ArticleVo> createArticle(Long userId, String title, String content, List<Long> tagIds, MultipartFile cover) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (title == null || title.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "请输入文章标题");
        }
        if (title.length() > 256) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "标题不能超过256个字符");
        }
        if (content == null || content.isBlank()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "请输入文章内容");
        }

        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(title.trim());
        article.setContent(content.trim());

        if (tagIds != null && !tagIds.isEmpty()) {
            TagVo firstTag = tagService.getTag(tagIds.get(0)).getData();
            article.setArticleType(firstTag != null ? firstTag.getName() : "other");
        }

        if (cover != null && !cover.isEmpty()) {
            String originalFilename = cover.getOriginalFilename();
            if (!"image".equals(FileTypeUtil.getFileType(originalFilename))) {
                return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "不支持的图片格式，仅支持 JPG、PNG、GIF、WebP");
            }
            if (cover.getSize() > 10 * 1024 * 1024) {
                return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "封面图片大小不能超过 10MB");
            }
        }

        int result = articleMapper.insertArticle(article);
        if (result <= 0) {
            return ResponseModel.serverError();
        }

        if (cover != null && !cover.isEmpty()) {
            String extension = FileTypeUtil.getFileExtension2(cover.getOriginalFilename());
            String objectKey = AppConstants.ARTICLE_COVER_PATH + "/" + article.getArticleId() + extension;
            try {
                rustFsService.upload(objectKey, cover.getInputStream(), cover.getSize(), cover.getContentType());
            } catch (IOException e) {
                log.error("Failed to upload article cover to RustFS", e);
                throw new RuntimeException("Failed to upload article cover", e);
            }
            articleMapper.updateCoverKey(article.getArticleId(), objectKey);
            article.setCoverKey(objectKey);
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                tagMapper.insertArticleTag(article.getArticleId(), tagId);
            }
        }

        return ResponseModel.success("文章发布成功", toArticleVo(article));
    }

    @Override
    public ResponseModel<List<ArticleVo>> listArticles(Long tagId) {
        List<Article> articles;
        if (tagId != null) {
            articles = articleMapper.selectByTagId(tagId, 0, Integer.MAX_VALUE);
        } else {
            articles = articleMapper.selectPublishedList();
        }
        List<Long> articleIds = articles.stream().map(Article::getArticleId).collect(Collectors.toList());
        Map<Long, List<TagVo>> tagsMap = batchLoadTags(articleIds);
        List<ArticleVo> voList = new ArrayList<>();
        for (Article article : articles) {
            voList.add(toArticleVo(article, tagsMap.get(article.getArticleId())));
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<List<ArticleVo>> getRandomArticles(Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        int offset = (p - 1) * ps;

        List<Article> articles = articleMapper.selectRandomPublishedList(offset, ps);
        List<Long> articleIds = articles.stream().map(Article::getArticleId).collect(Collectors.toList());
        Map<Long, List<TagVo>> tagsMap = batchLoadTags(articleIds);
        List<ArticleVo> voList = new ArrayList<>();
        for (Article article : articles) {
            voList.add(toArticleVo(article, tagsMap.get(article.getArticleId())));
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<List<ArticleVo>> listUserArticles(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<Article> articles = articleMapper.selectByUserId(userId);
        List<Long> articleIds = articles.stream().map(Article::getArticleId).collect(Collectors.toList());
        Map<Long, List<TagVo>> tagsMap = batchLoadTags(articleIds);
        List<ArticleVo> voList = new ArrayList<>();
        for (Article article : articles) {
            voList.add(toArticleVo(article, tagsMap.get(article.getArticleId())));
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<Void> deleteArticle(Long userId, Long articleId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        if (articleId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return ResponseModel.notFound();
        }
        if (article.getUserId() != userId) {
            return ResponseModel.error(ResponseModel.CODE_FORBIDDEN, "只能删除自己的文章");
        }
        articleMapper.updateStatus(articleId, 0);
        if (article.getCoverKey() != null && !article.getCoverKey().isEmpty()) {
            try {
                rustFsService.deleteObject(article.getCoverKey());
            } catch (Exception e) {
                log.warn("Failed to delete cover from RustFS: {}", article.getCoverKey(), e);
            }
        }
        return ResponseModel.success("删除成功");
    }

    @Override
    public ResponseModel<ArticleVo> getArticle(Long articleId) {
        if (articleId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return ResponseModel.notFound();
        }
        return ResponseModel.success(toArticleVo(article));
    }

    @Override
    public ResponseModel<PageResult<ArticleVo>> listArticles(int page, int size, String keyword, Integer status, Long tagId) {
        int p = Math.max(page, 1);
        int s = Math.max(size, 1);
        int offset = (p - 1) * s;

        List<Article> records = articleMapper.selectByPageWithFilter(offset, s, keyword, status, tagId);
        long total = articleMapper.countWithFilter(keyword, status, tagId);
        List<Long> articleIds = records.stream().map(Article::getArticleId).collect(Collectors.toList());
        Map<Long, List<TagVo>> tagsMap = batchLoadTags(articleIds);
        List<ArticleVo> vos = records.stream().map(r -> toArticleVo(r, tagsMap.get(r.getArticleId()))).toList();
        return ResponseModel.success(new PageResult<>(vos, total, p, s));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseModel<ArticleVo> adminUpdateArticle(Long articleId, Article article, List<Long> tagIds) {
        if (articleId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        Article existing = articleMapper.selectById(articleId);
        if (existing == null) {
            return ResponseModel.notFound();
        }
        article.setArticleId(articleId);
        articleMapper.updateArticle(article);
        if (tagIds != null) {
            tagMapper.deleteArticleTags(articleId);
            for (Long tagId : tagIds) {
                tagMapper.insertArticleTag(articleId, tagId);
            }
        }
        Article updated = articleMapper.selectById(articleId);
        return ResponseModel.success("更新成功", toArticleVo(updated));
    }

    @Override
    public ResponseModel<Void> adminDeleteArticle(Long articleId) {
        if (articleId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return ResponseModel.notFound();
        }
        articleMapper.updateStatus(articleId, 0);
        if (article.getCoverKey() != null && !article.getCoverKey().isEmpty()) {
            try {
                rustFsService.deleteObject(article.getCoverKey());
            } catch (Exception e) {
                log.warn("Failed to delete cover from RustFS: {}", article.getCoverKey(), e);
            }
        }
        return ResponseModel.success("删除成功");
    }

    @Override
    public ResponseModel<Void> adminBatchUpdateArticles(BatchArticleDto dto) {
        if (dto.getArticleIds() == null || dto.getArticleIds().isEmpty()) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "请选择至少一篇文章");
        }
        if (dto.getStatus() != null) {
            for (Long articleId : dto.getArticleIds()) {
                articleMapper.updateStatus(articleId, dto.getStatus());
            }
            return ResponseModel.success("批量操作成功");
        } else {
            for (Long articleId : dto.getArticleIds()) {
                Article article = articleMapper.selectById(articleId);
                if (article != null) {
                    articleMapper.updateStatus(articleId, 0);
                    if (article.getCoverKey() != null && !article.getCoverKey().isEmpty()) {
                        try {
                            rustFsService.deleteObject(article.getCoverKey());
                        } catch (Exception e) {
                            log.warn("Failed to delete cover from RustFS: {}", article.getCoverKey(), e);
                        }
                    }
                }
            }
            return ResponseModel.success("批量删除成功");
        }
    }

    private ArticleVo toArticleVo(Article article) {
        return toArticleVo(article, tagService.getTagsByArticleId(article.getArticleId()));
    }

    private ArticleVo toArticleVo(Article article, List<TagVo> tags) {
        ArticleVo vo = new ArticleVo();
        vo.setArticleId(article.getArticleId());
        vo.setUserId(article.getUserId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setArticleType(article.getArticleType());
        vo.setTags(tags != null ? tags : new ArrayList<>());
        vo.setStatus(article.getStatus());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());
        vo.setLikeCount(article.getLikeCount());

        if (article.getCoverKey() != null && !article.getCoverKey().isEmpty()) {
            try {
                vo.setCoverUrl(rustFsService.presignedGetUrl(article.getCoverKey(), ARTICLE_URL_TIMEOUT_MINUTES));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for cover key: {}", article.getCoverKey(), e);
            }
        }

        User author = userMapper.selectById(article.getUserId());
        if (author != null) {
            vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getEmail());
            String avatarValue = author.getUserAvatar();
            if (avatarValue != null && !avatarValue.isEmpty() && !avatarValue.contains("://")) {
                try {
                    vo.setAuthorAvatar(rustFsService.presignedGetUrl(avatarValue, ARTICLE_URL_TIMEOUT_MINUTES));
                } catch (Exception e) {
                    log.warn("Failed to generate presigned URL for author avatar", e);
                }
            } else if (avatarValue != null && avatarValue.contains("://")) {
                vo.setAuthorAvatar(avatarValue);
            }
        }

        return vo;
    }

    private Map<Long, List<TagVo>> batchLoadTags(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        Map<Long, List<TagVo>> result = new java.util.HashMap<>();
        articleIds.forEach(id -> result.put(id, new ArrayList<>()));
        List<cc.ztzhome.zblog.bean.entity.Tag> tagsWithArticle = tagMapper.selectByArticleIds(articleIds);
        for (cc.ztzhome.zblog.bean.entity.Tag t : tagsWithArticle) {
            TagVo vo = new TagVo();
            vo.setTagId(t.getTagId());
            vo.setName(t.getName());
            vo.setIcon(t.getIcon());
            vo.setKeywords(t.getKeywords());
            result.get(t.getArticleId()).add(vo);
        }
        return result;
    }
}
