package com.github.offercat.cache.extra;

import java.io.Serializable;
import java.util.Map;

/**
 * 缓存对象解析器
 *
 * @author 徐通 xutong34
 * @since 2020年03月29日 18:03:25
 */
public interface CacheEntityParser {

    /**
     * 将内置缓存对象转化为对象
     * Convert built-in cache object to object
     *
     * @param cacheEntity built-in cache object
     * @return object
     */
    <T extends Serializable> T toObject(CacheEntity cacheEntity);

    /**
     * 将对象转化为内置缓存对象
     * Convert object to built-in cache object
     *
     * @param obj object
     * @param time time of create object
     * @return built-in cache object
     */
    <T extends Serializable> CacheEntity toCacheEntity(T obj, long time);

    /**
     *
     * @param cacheEntityMap
     * @param <T>
     * @return
     */
    <T extends Serializable> Map<String, T> toObjectMap(Map<String, CacheEntity> cacheEntityMap);

    /**
     *
     * @param keyValues
     * @param time
     * @param <T>
     * @return
     */
    <T extends Serializable> Map<String, CacheEntity> toCacheEntityMap(Map<String, T> keyValues, long time);

    /**
     * 将其他的缓存实体转化成本类型缓存实体
     *
     * @param originEntity cacheEntity
     * @param originParser
     * @return 本类型缓存实体
     */
    <T extends Serializable> CacheEntity transferCacheEntity(CacheEntity originEntity, CacheEntityParser originParser);


    /**
     *
     * @param originEntityMap
     * @param originParser
     * @return
     */
    <T extends Serializable> Map<String, CacheEntity> transferCacheEntityMap(Map<String, CacheEntity> originEntityMap,
                                                                             CacheEntityParser originParser);
}
