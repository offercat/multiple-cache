package com.github.offercat.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 中间件创建器
 * Middleware connection Creator
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 16:01:09
 */
@Slf4j
public class MiddlewareCreator {

    /**
     * 创建 Caffeine 本地缓存
     * Create Caffeine local cache
     *
     * @param itemProperties Caffeine cache properties
     * @return Caffeine cache
     */
    public static Cache<String, Object> createCaffeine(ItemProperties itemProperties) {
        log.info("Init Caffeine local cache, uniform expiration time is {} {}", itemProperties.getTimeout(), itemProperties.getTimeunit());
        Cache<String, Object> caffeine;
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .initialCapacity(itemProperties.getMaxSize() / 2)
                .maximumSize(itemProperties.getMaxSize());
        if (ItemProperties.ExpireMode.TTL == itemProperties.getExpireMode()) {
            caffeine = caffeineBuilder.expireAfterWrite(
                    itemProperties.getTimeout(),
                    itemProperties.getTimeunit()
            ).build();
        } else {
            caffeine = caffeineBuilder.expireAfterAccess(
                    itemProperties.getTimeout(),
                    itemProperties.getTimeunit()
            ).build();
        }
        return caffeine;
    }

    /**
     * 创建 Jedis 连接池
     * Create JedisPool
     *
     * @param itemProperties Redis cache properties
     * @return JedisPool
     */
    public static JedisPool createJedisPool(ItemProperties itemProperties) {
        log.info("Init redis cluster cache, uniform expiration time is {} {}", itemProperties.getTimeout(), itemProperties.getTimeunit());
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMinIdle(10);
        poolConfig.setMaxIdle(30);
        return new JedisPool(
                poolConfig,
                itemProperties.getAddress(),
                itemProperties.getPort(),
                10000,
                itemProperties.getPassword()
        );
    }
}
