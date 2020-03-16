package com.github.offercat.cache.config;

import com.github.offercat.cache.broadcast.BroadcastService;
import com.github.offercat.cache.broadcast.BroadcastServiceImpl;
import com.github.offercat.cache.inte.*;
import com.github.offercat.cache.proxy.CacheAspect;
import com.github.offercat.cache.proxy.CommonProxy;
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
public abstract class CacheFactory {

    protected CacheProperties properties;
    protected Serializer serializer;
    private static List<AbstractCache> REGISTER_CACHE_LIST = new ArrayList<>();

    protected <T extends AbstractCache> T initInstance(T target) {
        BroadcastService broadcastService = properties.isBroadcastEnable() ?
                new BroadcastServiceImpl(properties, serializer, REGISTER_CACHE_LIST) : null;

        CommonProxy<T> proxy = new CommonProxy<>();
        T cache = proxy.getProxy(target, new CacheAspect(target, properties, broadcastService));
        REGISTER_CACHE_LIST.add(cache);
        return cache;
    }

    public abstract LocalCache getLocalCacheInstance();

    public abstract ClusterCache getClusterCacheInstance();

    public abstract DirectCache getDirectCacheInstance();
}
