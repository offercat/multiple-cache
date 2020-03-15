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
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 01:15:39
 */
public interface MultipleCache {

    /**
     * 逐级获取对象后回填
     *
     * @param key      key
     * @param callback 回源函数
     * @return 对象
     */
    <T extends Serializable> T get(String key, Supplier<T> callback);

    /**
     * 逐级获取对象后回填，null值回填
     *
     * @param key      key
     * @param callback 回源函数
     * @return 对象
     */
    <T extends Serializable> T get(String key, T nullValue, Supplier<T> callback);


    /**
     * 获取多个对象
     *
     * @param objectIds        对象的识别主键集合
     * @param cacheKeyGenerate 缓存key生成函数
     * @param getMulFunction   回源函数
     * @return 对象
     */
    <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                   CacheKeyGenerate<V> cacheKeyGenerate,
                                                   GetMulFunction<T, V> getMulFunction);


    /**
     * 获取多个对象，带null值回填
     *
     * @param objectIds        对象的识别主键集合
     * @param nullValue        null值回填
     * @param cacheKeyGenerate 缓存key生成函数
     * @param getMulFunction   回源函数
     * @return 对象
     */
    <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                   CacheKeyGenerate<V> cacheKeyGenerate,
                                                   T nullValue,
                                                   GetMulFunction<T, V> getMulFunction);

    /**
     * 逐级设置缓存对象
     *
     * @param key key
     * @param value 对象
     */
    <T extends Serializable> void set(String key, T value);


    /**
     * 批量逐级设置缓存对象
     *
     * @param keyValues 键值对映射
     */
    <T extends Serializable> void setMul(Map<String, T> keyValues);
}
