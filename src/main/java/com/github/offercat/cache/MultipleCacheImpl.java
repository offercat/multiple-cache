package com.github.offercat.cache;

import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.extra.*;
import com.github.offercat.cache.inte.AbstractCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

/**
 * 多级缓存实现
 * Multi level cache implementation
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 16:39:18
 */
@Slf4j
@AllArgsConstructor
public class MultipleCacheImpl implements MultipleCache {

    private AbstractCache beginNode;
    private CacheProperties properties;

    @Override
    public <T extends Serializable> T get(String key, Supplier<T> callback) {
        if (ObjectUtils.isEmpty(key)) {
            return null;
        }
        AbstractCache availableCache = this.getAvailableCache(beginNode);
        if (availableCache != null) {
            CacheEntity cacheEntity = this.get(key, availableCache);
            if (cacheEntity != null) {
                CacheEntityParser parser = availableCache.getCacheEntityParser();
                return parser.toObject(cacheEntity);
            }
        }
        if (callback == null) {
            return null;
        }
        T callbackValue = callback.get();
        if (callbackValue != null) {
            if (properties.isLogEnable()) {
                log.info("get | callback function got the object, key = {}", key);
            }
            this.set(key, callbackValue);
            return callbackValue;
        }
        return null;
    }

    @Override
    public <T extends Serializable> T get(String key, T nullValue, Supplier<T> callback) {
        if (ObjectUtils.isEmpty(key)) {
            return null;
        }
        AbstractCache availableCache = this.getAvailableCache(beginNode);
        CacheEntity cacheEntity = this.get(key, availableCache);
        if (cacheEntity != null) {
            CacheEntityParser parser = availableCache.getCacheEntityParser();
            T value = parser.toObject(cacheEntity);
            if (value.equals(nullValue)) {
                return null;
            }
            return value;
        }
        if (callback == null) {
            this.set(key, nullValue);
            return null;
        }
        T callbackValue = callback.get();
        if (callbackValue != null) {
            if (properties.isLogEnable()) {
                log.info("get | callback function got the object, key = {}", key);
            }
            this.set(key, callbackValue);
            return callbackValue;
        }
        if (properties.isLogEnable()) {
            log.info("get | backfill null value {}", nullValue);
        }
        this.set(key, nullValue);
        return null;
    }

    private <T extends Serializable> CacheEntity get(String key, AbstractCache node) {
        if (node == null) {
            return null;
        }
        CacheEntity cacheEntity = node.getCacheEntity(key);
        if (cacheEntity != null) {
            if (properties.isLogEnable()) {
                log.info("get | {} got the object, key = {}", node.getName(), key);
            }
            return cacheEntity;
        }
        AbstractCache nextAvailableCache = this.getAvailableCache(node.getNext());
        CacheEntity nextObject = this.get(key, nextAvailableCache);
        if (nextObject != null) {
            CacheEntityParser nextParser = nextAvailableCache.getCacheEntityParser();
            CacheEntity thisObject = node.getCacheEntityParser().transferCacheEntity(nextObject, nextParser);
            node.setWithBroadcast(key, thisObject);
            return thisObject;
        }
        return null;
    }

    @Override
    public <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                          CacheKeyGenerate<V> cacheKeyGenerate,
                                                          GetMulFunction<T, V> getMulFunction) {
        return this.getMul(objectIds, cacheKeyGenerate, getMulFunction, false, null).values();
    }


    @Override
    public <T extends CacheId<V>, V> Collection<T> getMul(Collection<V> objectIds,
                                                          CacheKeyGenerate<V> cacheKeyGenerate, T nullValue,
                                                          GetMulFunction<T, V> getMulFunction) {
        return this.getMul(objectIds, cacheKeyGenerate, getMulFunction, true, nullValue).values();
    }

    private <T extends CacheId<V>, V> Map<String, T> getMul(Collection<V> objectIds,
                                                          CacheKeyGenerate<V> cacheKeyGenerate,
                                                          GetMulFunction<T, V> getMulFunction,
                                                           boolean fillBack, T nullValue) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return new HashMap<>(0);
        }
        Map<String, V> keyIdMap = new HashMap<>(objectIds.size(), 2);
        objectIds.forEach(objectId -> keyIdMap.put(cacheKeyGenerate.get(objectId), objectId));

        Map<String, T> resultMap = new HashMap<>(objectIds.size(), 2);

        List<String> keys = new ArrayList<>(keyIdMap.keySet());
        AbstractCache availableCache = this.getAvailableCache(beginNode);
        if (availableCache != null) {
            Map<String, CacheEntity> cacheObjectMap = this.getMul(keys, availableCache);
            CacheEntityParser parser = availableCache.getCacheEntityParser();
            if (keys.size() == 0 || getMulFunction == null) {
                return parser.toObjectMap(cacheObjectMap);
            }
            resultMap.putAll(parser.toObjectMap(cacheObjectMap));
        }

        List<V> unResolveIds = new ArrayList<>();
        keys.forEach(key -> unResolveIds.add(keyIdMap.get(key)));
        Collection<T> others = getMulFunction.get(unResolveIds);

        if (!CollectionUtils.isEmpty(others)) {
            if (properties.isLogEnable()) {
                log.info("getMul | callback function got {} objects", others.size());
            }
            Map<String, T> callbackMap = new HashMap<>(others.size());
            others.forEach(item -> callbackMap.put(cacheKeyGenerate.get(item.getObjectId()), item));
            this.setMul(callbackMap);
            resultMap.putAll(callbackMap);
            keys.removeAll(callbackMap.keySet());
        }
        // null 值回填
        if (fillBack && keys.size() > 0) {
            if (properties.isLogEnable()) {
                log.info("getMul | backfill null value, size = {}", keys.size());
            }
            Map<String, T> nullValueMap = new HashMap<>(keys.size(), 2);
            keys.forEach(key -> nullValueMap.put(key, nullValue));
            this.setMul(nullValueMap);
        }
        return resultMap;
    }

    private Map<String, CacheEntity> getMul(List<String> keys, AbstractCache node) {
        if (node == null) {
            return new HashMap<>(0);
        }
        Map<String, CacheEntity> resultMap = node.getMulCacheEntity(keys);
        resultMap = resultMap == null ? new HashMap<>(keys.size(), 2) : resultMap;
        if (properties.isLogEnable() && resultMap.size() > 0) {
            log.info("getMul | {} got {} objects", node.getName(), resultMap.size());
        }
        keys.removeAll(resultMap.keySet());
        if (keys.size() == 0) {
            return resultMap;
        }
        AbstractCache nextAvailableCache = this.getAvailableCache(node.getNext());
        Map<String, CacheEntity> nextResultMap = getMul(keys, nextAvailableCache);
        // 回填
        if (!CollectionUtils.isEmpty(nextResultMap)) {
            Map<String, CacheEntity> thisCacheObjectMap = this.backfillCacheObjectMap(nextResultMap, node, nextAvailableCache);
            resultMap.putAll(thisCacheObjectMap);
        }
        return resultMap;
    }

    private <T extends CacheId<V>, V> Map<String, CacheEntity> backfillCacheObjectMap(Map<String, CacheEntity> nextResultMap,
                                                                                      AbstractCache thisCache,
                                                                                      AbstractCache nextCache) {
        if (CollectionUtils.isEmpty(nextResultMap)) {
            return new HashMap<>(0);
        }
        Map<String, CacheEntity> thisCacheObjectMap = new HashMap<>(nextResultMap.size(), 2);
        nextResultMap.forEach((key, nextObject) -> {
            CacheEntityParser nextParser = nextCache.getCacheEntityParser();
            CacheEntity thisObject = thisCache.getCacheEntityParser().transferCacheEntity(nextObject, nextParser);
            thisCacheObjectMap.put(key, thisObject);
        });
        thisCache.setMulWithBroadcast(thisCacheObjectMap);
        return thisCacheObjectMap;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        AbstractCache availableCache = this.getAvailableCache(beginNode);
        CacheEntity cacheEntity = availableCache.getCacheEntityParser().toCacheEntity(value, System.currentTimeMillis());
        this.set(key, cacheEntity, beginNode);
    }

    private void set(String key, CacheEntity cacheEntity, AbstractCache availableCache) {
        if (StringUtils.isEmpty(key) || cacheEntity == null || availableCache == null) {
            return;
        }
        availableCache.setWithBroadcast(key, cacheEntity);
        AbstractCache nextAvailableCache = this.getAvailableCache(availableCache.getNext());
        if (nextAvailableCache == null) {
            return;
        }
        CacheEntityParser nextParser = nextAvailableCache.getCacheEntityParser();
        CacheEntity nextCacheEntity = nextParser.transferCacheEntity(cacheEntity, availableCache.getCacheEntityParser());
        this.set(key, nextCacheEntity, nextAvailableCache);
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> keyValues) {
        AbstractCache availableCache = this.getAvailableCache(beginNode);
        CacheEntityParser cacheEntityParser = availableCache.getCacheEntityParser();
        Map<String, CacheEntity> cacheEntityMap = cacheEntityParser.toCacheEntityMap(keyValues, System.currentTimeMillis());
        this.setMul(cacheEntityMap, beginNode);
    }

    private void setMul(Map<String, CacheEntity> cacheEntityMap, AbstractCache availableCache) {
        if (CollectionUtils.isEmpty(cacheEntityMap) || availableCache == null) {
            return;
        }
        availableCache.setMulWithBroadcast(cacheEntityMap);
        AbstractCache nextAvailableCache = this.getAvailableCache(availableCache.getNext());
        if (nextAvailableCache == null) {
            return;
        }
        CacheEntityParser nextParser = nextAvailableCache.getCacheEntityParser();
        Map<String, CacheEntity> nextCacheEntityMap = nextParser.transferCacheEntityMap(cacheEntityMap, availableCache.getCacheEntityParser());
        this.setMul(nextCacheEntityMap, nextAvailableCache);
    }

    private AbstractCache getAvailableCache(AbstractCache node) {
        if (node == null) {
            return null;
        }
        if (node.getItemProperties().isEnable()) {
            return node;
        }
        return getAvailableCache(node.getNext());
    }
}
