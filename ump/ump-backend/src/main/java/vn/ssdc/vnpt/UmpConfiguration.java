package vn.ssdc.vnpt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.core.SsdcRedisCache;
import vn.vnpt.ssdc.core.SsdcCache;
import vn.vnpt.ssdc.jdbc.factories.RepositoryFactory;

import javax.sql.DataSource;

/**
 * Created by vietnq on 10/25/16.
 */
@Configuration
public class UmpConfiguration {

    @Bean
    @ConfigurationProperties(prefix="spring.datasource")
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SsdcCache ssdcCache(RedisTemplate redisTemplate) {
        return new SsdcRedisCache(redisTemplate);
    }

    @Bean
    public RepositoryFactory repositoryFactory(DataSource dataSource, SsdcCache ssdcCache) {
        return new RepositoryFactory(dataSource, ssdcCache);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
