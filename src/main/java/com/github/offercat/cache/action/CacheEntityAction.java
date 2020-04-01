package com.github.offercat.cache.action;

import com.github.offercat.cache.extra.CacheEntity;

import java.util.List;
import java.util.Map;

/**
 * 缓存对象行为
 *
 * @author 徐通 xutong34
 * @since 2020年03月29日 18:28:04
 */
public interface CacheEntityAction {

    /**
     * 存储内置缓存实体
     * Save built-in cache entity
     *
     * @param key         cache key
     * @param cacheEntity 缓存对象
     */
    void setCacheEntity(String key, CacheEntity cacheEntity);

    /**
     * 批量存储内置缓存对象
     * Batch save built-in cache entities
     *
     * @param keyObjects key-cacheEntity mapping
     */
    void setMulCacheEntity(Map<String, CacheEntity> keyObjects);

    /**
     * 获取内置缓存对象
     * Get built-in cache entity
     *
     * @param key cache key
     * @return built-in cache entity
     */
    CacheEntity getCacheEntity(String key);

    /**
     * 批量获取内置缓存对象
     * Get multiple built-in cache entities
     *
     * @param keys cache key list
     * @return key-cacheObject mapping
     */
    Map<String, CacheEntity> getMulCacheEntity(List<String> keys);
}
