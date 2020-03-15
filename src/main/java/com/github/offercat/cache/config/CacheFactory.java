package com.github.offercat.cache.config;

import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.DirectCache;
import com.github.offercat.cache.inte.LocalCache;
import com.github.offercat.cache.inte.Serializer;
import com.github.offercat.cache.ready.CaffeineCache;
import com.github.offercat.cache.ready.EhDirectCache;
import com.github.offercat.cache.ready.RedisCache;
import lombok.AllArgsConstructor;

/**
 * 缓存服务工厂
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 16:49:54
 */
@AllArgsConstructor
public class CacheFactory {

    private CacheProperties properties;
    private Serializer serializer;

    public LocalCache getLocalCacheInstance() {
        return new CaffeineCache("local", properties);
    }

    public ClusterCache getClusterCacheInstance() {
        return new RedisCache("cluster", serializer, properties);
    }

    public DirectCache getDirectCacheInstance() {
        return new EhDirectCache("direct", properties);
    }
}
