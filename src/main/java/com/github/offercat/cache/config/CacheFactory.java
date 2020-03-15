package com.github.offercat.cache.config;

import com.github.offercat.cache.broadcast.BroadcastService;
import com.github.offercat.cache.broadcast.BroadcastServiceImpl;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.inte.*;
import com.github.offercat.cache.proxy.CacheAspect;
import com.github.offercat.cache.proxy.CommonProxy;
import com.github.offercat.cache.ready.CaffeineCache;
import com.github.offercat.cache.ready.EhDirectCache;
import com.github.offercat.cache.ready.RedisCache;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存工厂，用于创建本地缓存、直接缓存、集群缓存
 * Cache factory, used to create local cache, direct cache, cluster cache
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 16:49:54
 */
@AllArgsConstructor
public class CacheFactory {

    private CacheProperties properties;
    private Serializer serializer;
    private static List<AbstractCache> REGISTER_CACHE_LIST = new ArrayList<>();

    public <T extends AbstractCache> T getCacheInstance(String cacheName, Class<T> cacheType) {
        ExceptionUtil.paramNull(cacheName, "Cache name can not be null!");
        ExceptionUtil.paramNull(cacheType, "Cache type can not be null!");
        ItemProperties itemProperties = properties.getConfig().get(cacheName);
        ExceptionUtil.paramNull(cacheType, "No cache properties matching " + cacheName + " found!");

        BroadcastService broadcastService = properties.isBroadcastEnable()
                ? new BroadcastServiceImpl(properties, serializer, REGISTER_CACHE_LIST) : null;

        CommonProxy<T> proxy = new CommonProxy<>();
        T cache = proxy.getProxy(
                cacheType,
                new CacheAspect(cacheName, properties, broadcastService),
                cacheName,
                serializer,
                itemProperties
        );
        REGISTER_CACHE_LIST.add(cache);
        return cache;
    }

    public LocalCache getLocalCacheInstance() {
        return this.getCacheInstance("local", CaffeineCache.class);
    }

    public ClusterCache getClusterCacheInstance() {
        return this.getCacheInstance("cluster", RedisCache.class);
    }

    public DirectCache getDirectCacheInstance() {
        return this.getCacheInstance("direct", EhDirectCache.class);
    }
}
