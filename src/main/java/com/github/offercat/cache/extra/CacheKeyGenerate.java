package com.github.offercat.cache.extra;

/**
 * 缓存 key 生成策略
 * Cache key generation strategy
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019/10/10 12:10
 */
@FunctionalInterface
public interface CacheKeyGenerate<V> {

    /**
     * 提供对象的唯一标识，返回生成的缓存key
     * Provide the unique ID of the object and return the generated cache key
     *
     * @param objectId object unique ID
     * @return cache key
     */
    String get(V objectId);
}
