package com.github.offercat.cache.ready;

import com.github.offercat.cache.config.CacheFactory;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.extra.SerializeStringEntityParser;
import com.github.offercat.cache.extra.UnSerializeEntityParser;
import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.DirectCache;
import com.github.offercat.cache.inte.LocalCache;
import com.github.offercat.cache.inte.Serializer;

/**
 * 三级缓存工厂
 *
 * @author 徐通 xutong34
 * @since 2020/3/16 12:46
 */
public class ThreeLevelCacheFactory extends CacheFactory {

    public ThreeLevelCacheFactory(CacheProperties properties, Serializer serializer) {
        super(properties, serializer);
    }

    @Override
    public LocalCache getLocalCacheInstance() {
        ItemProperties itemProperties = properties.getConfig().get("local");
        CaffeineCache caffeineCache = new CaffeineCache("local", itemProperties);
        return this.initInstance(caffeineCache);
    }

    @Override
    public ClusterCache getClusterCacheInstance() {
        ItemProperties itemProperties = properties.getConfig().get("cluster");
        RedisCache redisCache = new RedisCache("cluster", serializer, itemProperties);
        redisCache.setCacheEntityParser(new SerializeStringEntityParser(serializer));
        return this.initInstance(redisCache);
    }

    @Override
    public DirectCache getDirectCacheInstance() {
        ItemProperties itemProperties = properties.getConfig().get("direct");
        EhDirectCache ehDirectCache = new EhDirectCache("direct", serializer, itemProperties);
        ehDirectCache.setCacheEntityParser(new SerializeStringEntityParser(serializer));
        return this.initInstance(ehDirectCache);
    }
}
