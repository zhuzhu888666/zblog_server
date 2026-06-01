package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.entity.ArticleLike;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.mapper.ArticleLikeMapper;
import cc.ztzhome.zblog.mapper.ArticleMapper;
import cc.ztzhome.zblog.service.IArticleLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleLikeServiceImpl implements IArticleLikeService {

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public ResponseModel<Boolean> toggleLike(Long userId, Long articleId) {
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

        ArticleLike exists = articleLikeMapper.selectByUserAndArticle(userId, articleId);
        if (exists != null) {
            articleLikeMapper.deleteByUserAndArticle(userId, articleId);
            return ResponseModel.success("取消点赞", false);
        }

        ArticleLike articleLike = new ArticleLike();
        articleLike.setUserId(userId);
        articleLike.setArticleId(articleId);
        articleLikeMapper.insert(articleLike);
        return ResponseModel.success("点赞成功", true);
    }

    @Override
    public ResponseModel<Boolean> isLiked(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            return ResponseModel.success(false);
        }
        ArticleLike exists = articleLikeMapper.selectByUserAndArticle(userId, articleId);
        return ResponseModel.success(exists != null);
    }

    @Override
    public ResponseModel<Integer> getLikeCount(Long articleId) {
        if (articleId == null) {
            return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        int count = articleLikeMapper.countByArticleId(articleId);
        return ResponseModel.success(count);
    }
}
