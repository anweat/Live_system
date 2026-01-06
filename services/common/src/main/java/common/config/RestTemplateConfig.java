package common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 * 用于微服务间的 HTTP 调用
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置 RestTemplate Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 可以在这里添加拦截器、错误处理等
        return restTemplate;
    }
}
