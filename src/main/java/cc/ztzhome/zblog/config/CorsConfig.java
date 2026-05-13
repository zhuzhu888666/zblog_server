package cc.ztzhome.zblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        // 1. 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 允许所有来源（本地开发用，生产环境务必替换为具体域名）
        config.addAllowedOriginPattern("*");
        // 3. 允许所有请求头
        config.addAllowedHeader("*");
        // 4. 允许所有 HTTP 方法 (GET, POST, PUT, DELETE...)
        config.addAllowedMethod("*");
        // 5. 允许请求携带凭证（如 Cookie）
        config.setAllowCredentials(true);
        // 6. 设置预检请求的缓存时间 (单位: 秒)
        config.setMaxAge(3600L);

        // 7. 为所有接口 (/**, 代表所有路径) 应用以上配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 8. 返回 CorsFilter 对象
        return new CorsFilter(source);
    }
}