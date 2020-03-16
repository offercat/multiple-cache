package com.github.offercat.cache.action;

import com.github.offercat.cache.extra.CacheObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 缓存接口，通用缓存接口
 * Basic cache interface, all subclasses need to implement it
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019年12月08日 2:57
 */
public interface BaseAction {

    /**
     * 获取对象
     * Get object
     *
     * @param key cache key
     * @return object
     */
    <T extends Serializable> T get(String key);

    /**
     * 批量获取对象
     * Get multiple objects
     *
     * @param keys cache key list
     * @return multiple objects
     */
    <T extends Serializable> Map<String, T> getMul(List<String> keys);

    /**
     * 存储对象
     * Save object
     *
     * @param key   cache key
     * @param value object
     */
    <T extends Serializable> void set(String key, T value);

    /**
     * 设置多个对象
     * Save multiple objects
     *
     * @param keyValues key-value mapping
     */
    <T extends Serializable> void setMul(Map<String, T> keyValues);

    /**
     * 删除对象
     * Delete object
     *
     * @param key cache key
     */
    void del(String key);

    /**
     * 批量删除
     * Delete multiple objects
     *
     * @param keys cache key list
     */
    void delMul(List<String> keys);

    /**
     * 存储内置缓存对象
     * Save built-in cache object
     *
     * @param key         cache key
     * @param cacheObject 缓存对象
     */
    void setCacheObject(String key, CacheObject cacheObject);

    /**
     * 批量存储内置缓存对象
     * Batch save built-in cache objects
     *
     * @param keyObjects key-cacheObject mapping
     */
    void setMulCacheObject(Map<String, CacheObject> keyObjects);

    /**
     * 获取内置缓存对象
     * Get built-in cache object
     *
     * @param key cache key
     * @return built-in cache object
     */
    CacheObject getCacheObject(String key);

    /**
     * 批量获取内置缓存对象
     * Get multiple built-in cache objects
     *
     * @param keys cache key list
     * @return key-cacheObject mapping
     */
    Map<String, CacheObject> getMulCacheObject(List<String> keys);

    <T extends Serializable> T transfer(CacheObject cacheObject);

    <T extends Serializable> CacheObject transfer(T obj, long time);
}
