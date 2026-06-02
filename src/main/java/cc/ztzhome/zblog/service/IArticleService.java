package cc.ztzhome.zblog.service;

import cc.ztzhome.zblog.bean.dto.BatchArticleDto;
import cc.ztzhome.zblog.bean.entity.Article;
import cc.ztzhome.zblog.bean.response.ResponseModel;
import cc.ztzhome.zblog.bean.vo.ArticleVo;
import cc.ztzhome.zblog.bean.vo.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleService {
    ResponseModel<ArticleVo> createArticle(Long userId, String title, String content, List<Long> tagIds, MultipartFile cover);

    ResponseModel<List<ArticleVo>> listArticles(Long tagId);

    ResponseModel<ArticleVo> getArticle(Long articleId);

    ResponseModel<List<ArticleVo>> getRandomArticles(Integer page, Integer pageSize);

    ResponseModel<List<ArticleVo>> listUserArticles(Long userId);

    ResponseModel<Void> deleteArticle(Long userId, Long articleId);

    ResponseModel<PageResult<ArticleVo>> listArticles(int page, int size, String keyword, Integer status, Long tagId);

    ResponseModel<ArticleVo> adminUpdateArticle(Long articleId, Article article, List<Long> tagIds);

    ResponseModel<Void> adminDeleteArticle(Long articleId);

    ResponseModel<Void> adminBatchUpdateArticles(BatchArticleDto dto);
}
