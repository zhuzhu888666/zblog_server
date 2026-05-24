package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.ArticleMapper;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IArticleService;
import cc.ztzhome.zblog.service.RustFsService;
import cc.ztzhome.zblog.utils.FileTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ArticleServiceImpl implements IArticleService {

    private static final long ARTICLE_URL_TIMEOUT_MINUTES = AppConstants.URL_TIMEOUT;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RustFsService rustFsService;

    @Override
    public ResponseModel<ArticleVo> createArticle(Long userId, String title, String content, MultipartFile cover) {
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
                return ResponseModel.serverError();
            }
            articleMapper.updateCoverKey(article.getArticleId(), objectKey);
            article.setCoverKey(objectKey);
        }

        return ResponseModel.success("文章发布成功", toArticleVo(article));
    }

    @Override
    public ResponseModel<List<ArticleVo>> listArticles() {
        List<Article> articles = articleMapper.selectPublishedList();
        List<ArticleVo> voList = new ArrayList<>();
        for (Article article : articles) {
            voList.add(toArticleVo(article));
        }
        return ResponseModel.success(voList);
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

    private ArticleVo toArticleVo(Article article) {
        ArticleVo vo = new ArticleVo();
        vo.setArticleId(article.getArticleId());
        vo.setUserId(article.getUserId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setStatus(article.getStatus());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

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
}
