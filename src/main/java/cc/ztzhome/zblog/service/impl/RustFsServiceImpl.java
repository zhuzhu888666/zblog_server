package cc.ztzhome.zblog.service.impl;

import cc.ztzhome.zblog.service.RustFsService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RustFS 对象存储服务实现（基于 AWS SDK v2，S3 兼容）
 * <p>封装文件上传/下载、预签名 URL 生成、对象管理等功能。</p>
 */
@Slf4j
@Service
public class RustFsServiceImpl implements RustFsService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;

    public RustFsServiceImpl(S3Client s3Client,
                             S3Presigner presigner,
                             @Qualifier("rustFsBucket") String bucket) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = bucket;
    }

    /**
     * 初始化：检查并自动创建存储桶
     */
    @PostConstruct
    public void init() {
        try {
            log.info("RustFS S3Client 初始化完成, bucket: {}", bucket);
            if (!bucketExists()) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("Bucket '{}' 已创建", bucket);
            }
        } catch (Exception e) {
            log.warn("RustFS 初始化失败，文件存储服务暂不可用: {}", e.getMessage());
        }
    }

    /**
     * 检查存储桶是否存在
     */
    private boolean bucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ======================== 文件上传 ========================

    @Override
    public void upload(String objectKey, Path filePath) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(objectKey).build(),
                    filePath
            );
            log.info("文件上传成功: {}", objectKey);
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectKey, e);
            throw new RuntimeException("文件上传失败: " + objectKey, e);
        }
    }

    @Override
    public void upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(contentType)
                            .contentLength(contentLength)
                            .build(),
                    RequestBody.fromInputStream(inputStream, contentLength)
            );
            log.info("流上传成功: {}", objectKey);
        } catch (Exception e) {
            log.error("流上传失败: {}", objectKey, e);
            throw new RuntimeException("流上传失败: " + objectKey, e);
        }
    }

    // ======================== 文件下载 ========================

    @Override
    public void download(String objectKey, Path targetPath) {
        try {
            s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(objectKey).build(),
                    targetPath
            );
            log.info("文件下载成功: {} -> {}", objectKey, targetPath);
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectKey, e);
            throw new RuntimeException("文件下载失败: " + objectKey, e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String objectKey) {
        try {
            byte[] bytes = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(objectKey).build(),
                    ResponseTransformer.toBytes()
            ).asByteArray();
            log.info("文件下载成功: {}, 大小: {} bytes", objectKey, bytes.length);
            return bytes;
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectKey, e);
            throw new RuntimeException("文件下载失败: " + objectKey, e);
        }
    }

    // ======================== 预签名 URL ========================

    @Override
    public String presignedGetUrl(String objectKey, long durationMinutes) {
        try {
            var getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            var presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(durationMinutes))
                    .build();

            String url = presigner.presignGetObject(presignRequest).url().toString();
            log.info("预签名 GET URL 已生成: {}, 有效期 {} 分钟", objectKey, durationMinutes);
            return url;
        } catch (Exception e) {
            log.error("生成预签名 GET URL 失败: {}", objectKey, e);
            throw new RuntimeException("生成预签名 GET URL 失败: " + objectKey, e);
        }
    }

    @Override
    public String presignedPutUrl(String objectKey, long durationMinutes) {
        return presignedPutUrl(objectKey, durationMinutes, null);
    }

    @Override
    public String presignedPutUrl(String objectKey, long durationMinutes, String contentType) {
        try {
            var putBuilder = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey);

            if (contentType != null && !contentType.isBlank()) {
                putBuilder.contentType(contentType);
            }

            var presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putBuilder.build())
                    .signatureDuration(Duration.ofMinutes(durationMinutes))
                    .build();

            String url = presigner.presignPutObject(presignRequest).url().toString();
            log.info("预签名 PUT URL 已生成: {}, 有效期 {} 分钟", objectKey, durationMinutes);
            return url;
        } catch (Exception e) {
            log.error("生成预签名 PUT URL 失败: {}", objectKey, e);
            throw new RuntimeException("生成预签名 PUT URL 失败: " + objectKey, e);
        }
    }

    // ======================== 对象管理 ========================

    @Override
    public List<String> listObjects() {
        return listObjects(null);
    }

    @Override
    public List<String> listObjects(String prefix) {
        try {
            var requestBuilder = ListObjectsV2Request.builder().bucket(bucket);
            if (prefix != null && !prefix.isBlank()) {
                requestBuilder.prefix(prefix);
            }

            List<String> keys = s3Client.listObjectsV2Paginator(requestBuilder.build())
                    .stream()
                    .flatMap(response -> response.contents().stream())
                    .map(S3Object::key)
                    .collect(Collectors.toList());

            log.info("列出 {} 个对象 (prefix: {})", keys.size(), prefix);
            return keys;
        } catch (Exception e) {
            log.error("列出对象失败", e);
            throw new RuntimeException("列出对象失败", e);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build()
            );
            log.info("对象已删除: {}", objectKey);
        } catch (Exception e) {
            log.error("删除对象失败: {}", objectKey, e);
            throw new RuntimeException("删除对象失败: " + objectKey, e);
        }
    }

    @Override
    public boolean objectExists(String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(objectKey).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("检查对象存在性失败: {}", objectKey, e);
            throw new RuntimeException("检查对象存在性失败: " + objectKey, e);
        }
    }
}
