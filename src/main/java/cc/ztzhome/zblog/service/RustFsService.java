package cc.ztzhome.zblog.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * RustFS 对象存储服务接口（S3 兼容）
 */
public interface RustFsService {

    /**
     * 上传本地文件
     * @param objectKey 对象键（存储路径）
     * @param filePath  本地文件路径
     */
    void upload(String objectKey, Path filePath);

    /**
     * 通过输入流上传文件
     * @param objectKey     对象键（存储路径）
     * @param inputStream   输入流
     * @param contentLength 内容长度（字节）
     * @param contentType   MIME 类型
     */
    void upload(String objectKey, InputStream inputStream, long contentLength, String contentType);

    /**
     * 下载文件到本地路径
     * @param objectKey  对象键
     * @param targetPath 目标路径
     */
    void download(String objectKey, Path targetPath);

    /**
     * 下载文件为字节数组（小文件适用）
     * @param objectKey 对象键
     * @return 文件字节数组
     */
    byte[] downloadAsBytes(String objectKey);

    /**
     * 生成预签名下载 URL
     * @param objectKey       对象键
     * @param durationMinutes 有效期（分钟）
     * @return 预签名 URL
     */
    String presignedGetUrl(String objectKey, long durationMinutes);

    /**
     * 生成预签名上传 URL（不限制 Content-Type）
     * @param objectKey       对象键
     * @param durationMinutes 有效期（分钟）
     * @return 预签名 URL
     */
    String presignedPutUrl(String objectKey, long durationMinutes);

    /**
     * 生成预签名上传 URL（限定 Content-Type）
     * @param objectKey       对象键
     * @param durationMinutes 有效期（分钟）
     * @param contentType     MIME 类型，为 null 时不限制
     * @return 预签名 URL
     */
    String presignedPutUrl(String objectKey, long durationMinutes, String contentType);

    /**
     * 列出存储桶中所有对象键
     * @return 对象键列表
     */
    List<String> listObjects();

    /**
     * 列出指定前缀的对象键
     * @param prefix 前缀筛选
     * @return 对象键列表
     */
    List<String> listObjects(String prefix);

    /**
     * 删除指定对象
     * @param objectKey 对象键
     */
    void deleteObject(String objectKey);

    /**
     * 检查对象是否存在
     * @param objectKey 对象键
     * @return true 存在，false 不存在
     */
    boolean objectExists(String objectKey);
}
