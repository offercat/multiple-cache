package com.github.offercat.cache;

import com.github.offercat.cache.extra.CacheId;
import com.github.offercat.cache.extra.CacheKeyGenerate;
import com.github.offercat.cache.extra.GetMulFunction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 多级缓存核心接口
 * Multi level cache core interface
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 01:15:39
 */
public interface MultipleCache {

    /**
     * 逐级获取对象后回填
     * Backfill after getting objects level by level
     *
     * @param key      cache key
     * @param callback callback function
     * @return 对象
     */
    <T extends Serializable> T get(String key, Supplier<T> callback);


    /**
     * 逐级获取对象后回填，带null值回填
     * Backfill after getting objects level by level, contains null value
     *
     * @param key       cache key
     * @param nullValue null value, prevent duplicate null cache penetration
     * @param callback  callback function
     * @return 对象
     */
    <T extends Serializable> T get(String key, T nullValue, Supplier<T> callback);


    /**
     * 获逐级批量获取对象后回填
     * Backfill after obtaining objects in batch level by level
     *
     * @param objectIds        object unique ID list
     * @param cacheKeyGenerate cache key generation strategy
     * @param getMulFunction   callback function
     * @return 对象
     */
    <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                   CacheKeyGenerate<V> cacheKeyGenerate,
                                                   GetMulFunction<T, V> getMulFunction);


    /**
     * 获逐级批量获取对象后回填，带null值回填
     * Backfill after obtaining objects in batch level by level, contains null value
     *
     * @param objectIds        object unique ID list
     * @param cacheKeyGenerate cache key generation strategy
     * @param nullValue        null value, prevent duplicate null cache penetration
     * @param getMulFunction   callback function
     * @return 对象
     */
    <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                   CacheKeyGenerate<V> cacheKeyGenerate,
                                                   T nullValue,
                                                   GetMulFunction<T, V> getMulFunction);


    /**
     * 逐级设置缓存对象
     * Set cache object level by level
     *
     * @param key   cache key
     * @param value object
     */
    <T extends Serializable> void set(String key, T value);


    /**
     * 批量逐级设置缓存对象
     * Set multi cache object level by level
     *
     * @param keyValues key-value mapping
     */
    <T extends Serializable> void setMul(Map<String, T> keyValues);

    /**
     * 逐级删除缓存对象
     * Del multi cache object level by level
     *
     * @param key cache key
     */
    void del(String key);

    /**
     * 逐级批量删除缓存对象
     *
     * @param keys cache key collection
     */
    void delMul(Collection<String> keys);
}
