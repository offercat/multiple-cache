package com.github.offercat.cache.config;

import com.github.offercat.cache.MultipleCache;
import com.github.offercat.cache.MultipleCacheImpl;
import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.DirectCache;
import com.github.offercat.cache.inte.LocalCache;
import com.github.offercat.cache.inte.Serializer;
import com.github.offercat.cache.ready.DefaultSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * spring boot 自动配置
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 01:09:34
 */
@Configuration
@ConditionalOnClass({MultipleCache.class})
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfig {

    @Resource
    private CacheProperties cacheProperties;

    @Bean
    @ConditionalOnMissingBean(Serializer.class)
    Serializer serializer() {
        return new DefaultSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(CacheFactory.class)
    CacheFactory cacheFactory(Serializer serializer) {
        return new CacheFactory(cacheProperties, serializer);
    }

    @Bean
    @ConditionalOnMissingBean(LocalCache.class)
    LocalCache localCache(CacheFactory cacheFactory) {
        return cacheFactory.getLocalCacheInstance();
    }

    @Bean
    @ConditionalOnMissingBean(ClusterCache.class)
    ClusterCache clusterCache(CacheFactory cacheFactory) {
        return cacheFactory.getClusterCacheInstance();
    }

    @Bean
    @ConditionalOnMissingBean(DirectCache.class)
    DirectCache directCache(CacheFactory cacheFactory) {
        return cacheFactory.getDirectCacheInstance();
    }

    @Bean
    @ConditionalOnMissingBean(MultipleCache.class)
    MultipleCache multipleCache(LocalCache localCache, DirectCache directCache, ClusterCache clusterCache) {
        localCache.setNext(directCache).setNext(clusterCache);
        return new MultipleCacheImpl(localCache, cacheProperties);
    }
}
