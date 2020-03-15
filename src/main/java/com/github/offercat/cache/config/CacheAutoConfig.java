package com.github.offercat.cache.config;

import com.github.offercat.cache.MultipleCache;
import com.github.offercat.cache.MultipleCacheImpl;
import com.github.offercat.cache.inte.*;
import com.github.offercat.cache.ready.DefaultSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * spring boot 自动配置
 * Automatically configure spring bean
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 01:09:34
 */
@Configuration
@ConditionalOnClass({MultipleCache.class})
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfig {

    @Resource
    private CacheProperties cacheProperties;

    /**
     * 序列化器，默认使用阿里巴巴 fastjson 序列化，可以自定义
     * Serializer, alibaba fastjson serialization is used by default, which can be customized
     */
    @Bean
    @ConditionalOnMissingBean(Serializer.class)
    Serializer serializer() {
        return new DefaultSerializer();
    }

    /**
     * 缓存工厂，可以重写它并返回自定义的工厂
     * Cache factory, which can be overridden and return a custom factory
     */
    @Bean
    @ConditionalOnMissingBean(CacheFactory.class)
    CacheFactory cacheFactory(Serializer serializer) {
        return new CacheFactory(cacheProperties, serializer);
    }

    /**
     * 本地缓存，默认使用 Caffeine 缓存，可以重写它并返回自定义的本地缓存
     * Local cache, the default is Caffeine cache, which can be overridden and returned to a custom local cache
     */
    @Bean
    @ConditionalOnMissingBean(LocalCache.class)
    LocalCache localCache(CacheFactory cacheFactory) {
        return cacheFactory.getLocalCacheInstance();
    }

    /**
     * 堆外直接缓存，默认使用 EhCache 缓存，可以重写它并返回自定义的直接缓存
     * Direct cache, the default is EhCache cache, which can be overridden and returned to a custom direct cache
     */
    @Bean
    @ConditionalOnMissingBean(DirectCache.class)
    DirectCache directCache(CacheFactory cacheFactory) {
        return cacheFactory.getDirectCacheInstance();
    }

    /**
     * 集群缓存，默认使用 Redis 缓存，可以重写它并返回自定义的集群缓存
     * Cluster cache, the default is Redis cache, which can be overridden and returned to a custom cluster cache
     */
    @Bean
    @ConditionalOnMissingBean(ClusterCache.class)
    ClusterCache clusterCache(CacheFactory cacheFactory) {
        return cacheFactory.getClusterCacheInstance();
    }

    /**
     * 多级缓存，默认集成 Caffeine EhCache Redis 三级缓存，可以重写它并返回自定义的多级缓存
     * Multilevel caching, which integrates Caffeine EhCache Redis three level cache by default, can rewrite it and return to a custom multilevel cache
     */
    @Bean
    @ConditionalOnMissingBean(MultipleCache.class)
    MultipleCache multipleCache(LocalCache localCache, DirectCache directCache, ClusterCache clusterCache) {
        localCache.setNext(directCache).setNext(clusterCache);
        return new MultipleCacheImpl(localCache, cacheProperties);
    }
}
