package com.github.offercat.cache.extra;

/**
 * 缓存 key 获取事件
 *
 * @author 徐通 xutong34
 * @since 2019/10/10 12:10
 */
@FunctionalInterface
public interface CacheKeyGenerate<V> {

    /**
     * 获取缓存 key
     *
     * @param objectId 对象 id
     * @return key
     */
    String get(V objectId);
}
