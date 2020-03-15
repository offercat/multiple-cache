package com.github.offercat.cache.broadcast;

import com.github.offercat.cache.extra.CacheObject;

import java.util.List;
import java.util.Map;

/**
 * 缓存广播相关接口
 * Cache broadcast related interface
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月15日 16:35:56
 */
public interface WithBroadcast {

    /**
     * 标记本缓存是否支持广播
     * Mark whether this cache supports broadcasting
     *
     * @return true/false
     */
    boolean supportBroadcast();

    /**
     * 存储内置缓存对象并广播
     * Save built-in cache object and broadcast
     *
     * @param key         cache key
     * @param cacheObject 缓存对象
     */
    void setWithBroadcast(String key, CacheObject cacheObject);

    /**
     * 批量存储内置缓存对象
     * Batch save built-in cache objects and broadcast
     *
     * @param keyObjects key-cacheObject mapping
     */
    void setMulWithBroadcast(Map<String, CacheObject> keyObjects);

    /**
     * 删除对象
     * Delete object and broadcast
     *
     * @param key cache key
     */
    void delWithBroadcast(String key);

    /**
     * 批量删除
     * Delete multiple objects and broadcast
     *
     * @param keys cache key list
     */
    void delMulWithBroadcast(List<String> keys);
}
