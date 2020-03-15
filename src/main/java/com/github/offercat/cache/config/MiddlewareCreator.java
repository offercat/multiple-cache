package com.github.offercat.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 中间件创建器
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 16:01:09
 */

@Slf4j
public class MiddlewareCreator {

    public static Cache<String, Object> createCaffeine(ItemProperties itemProperties) {
        log.info("初始化 caffeine 本地缓存，本地缓存统一过期时间为 {} {}", itemProperties.getTimeout(), itemProperties.getTimeunit());
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

    public static Jedis createJedis(ItemProperties itemProperties) {
        log.info("初始化 redis 集群缓存，统一过期时间为 {} {}", itemProperties.getTimeout(), itemProperties.getTimeunit());
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMinIdle(10);
        poolConfig.setMaxIdle(30);
        JedisPool jedisPool = new JedisPool(
                poolConfig,
                itemProperties.getAddress(),
                itemProperties.getPort(),
                10000,
                itemProperties.getPassword()
        );
        return jedisPool.getResource();
    }
}
