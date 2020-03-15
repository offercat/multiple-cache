package com.github.offercat.cache;

import com.github.offercat.cache.config.CacheFactory;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.inte.LocalCache;
import com.github.offercat.cache.ready.CaffeineCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * 测试启动类
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 20:37:55
 */
@SpringBootApplication
public class Application {

    @Autowired
    private CacheProperties properties;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    LocalCache localCache(CacheFactory cacheFactory) {
        ItemProperties localProperties = properties.getConfig().get("local");
        localProperties.setTimeout(1);
        localProperties.setTimeunit(TimeUnit.SECONDS);
        return cacheFactory.getLocalCacheInstance();
    }
}
