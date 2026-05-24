package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.entity.Favorite;
import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.constant.AppConstants;
import cc.ztzhome.zblog.mapper.ArticleMapper;
import cc.ztzhome.zblog.mapper.FavoriteMapper;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.service.IFavoriteService;
import cc.ztzhome.zblog.service.RustFsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FavoriteServiceImpl implements IFavoriteService {

    private static final long URL_TIMEOUT_MINUTES = AppConstants.URL_TIMEOUT;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RustFsService rustFsService;

    @Override
    public ResponseModel<Boolean> toggleFavorite(Long userId, Long articleId) {
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

        Favorite exists = favoriteMapper.selectByUserAndArticle(userId, articleId);
        if (exists != null) {
            favoriteMapper.deleteByUserAndArticle(userId, articleId);
            return ResponseModel.success("取消收藏", false);
        }

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setArticleId(articleId);
        favoriteMapper.insert(fav);
        return ResponseModel.success("收藏成功", true);
    }

    @Override
    public ResponseModel<List<ArticleVo>> listUserFavorites(Long userId) {
        if (userId == null) {
            return ResponseModel.error(ResponseModel.CODE_UNAUTHORIZED, "请先登录");
        }
        List<Favorite> favorites = favoriteMapper.selectByUserId(userId);
        List<ArticleVo> voList = new ArrayList<>();
        for (Favorite fav : favorites) {
            Article article = articleMapper.selectById(fav.getArticleId());
            if (article != null && article.getStatus() == 1) {
                voList.add(toArticleVo(article));
            }
        }
        return ResponseModel.success(voList);
    }

    @Override
    public ResponseModel<Boolean> isFavorited(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            return ResponseModel.success(false);
        }
        Favorite exists = favoriteMapper.selectByUserAndArticle(userId, articleId);
        return ResponseModel.success(exists != null);
    }

    private ArticleVo toArticleVo(Article article) {
        ArticleVo vo = new ArticleVo();
        vo.setArticleId(article.getArticleId());
        vo.setUserId(article.getUserId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setArticleType(article.getArticleType());
        vo.setStatus(article.getStatus());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        if (article.getCoverKey() != null && !article.getCoverKey().isEmpty()) {
            try {
                vo.setCoverUrl(rustFsService.presignedGetUrl(article.getCoverKey(), URL_TIMEOUT_MINUTES));
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
                    vo.setAuthorAvatar(rustFsService.presignedGetUrl(avatarValue, URL_TIMEOUT_MINUTES));
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
