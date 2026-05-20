package cc.ztzhome.zblog.config;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * RustFS S3 客户端配置
 * <p>创建并管理 S3Client、S3Presigner 等 Bean，统一注入到 Service 层使用。</p>
 */
@Configuration
public class RustFsS3Config {

    @Value("${rustfs.endpoint}")
    private String endpoint;

    @Value("${rustfs.access-key}")
    private String accessKey;

    @Value("${rustfs.secret-key}")
    private String secretKey;

    @Value("${rustfs.bucket}")
    private String bucket;

    private S3Client s3Client;
    private S3Presigner presigner;

    /**
     * S3 客户端 Bean（路径风格访问）
     */
    @Bean
    public S3Client s3Client() {
        var credentials = AwsBasicCredentials.create(accessKey, secretKey);
        var credentialsProvider = StaticCredentialsProvider.create(credentials);

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .forcePathStyle(true)
                .build();
        return this.s3Client;
    }

    /**
     * S3 预签名器 Bean（用于生成预签名 URL）
     */
    @Bean
    public S3Presigner s3Presigner() {
        var credentials = AwsBasicCredentials.create(accessKey, secretKey);
        var credentialsProvider = StaticCredentialsProvider.create(credentials);

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
        return this.presigner;
    }

    /**
     * 存储桶名称 Bean
     */
    @Bean
    public String rustFsBucket() {
        return bucket;
    }

    /**
     * 容器销毁时关闭 S3 客户端
     */
    @PreDestroy
    public void destroy() {
        if (presigner != null) {
            presigner.close();
        }
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
