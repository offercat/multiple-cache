package com.github.offercat.cache;

import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.extra.CacheId;
import com.github.offercat.cache.extra.CacheKeyGenerate;
import com.github.offercat.cache.extra.CacheObject;
import com.github.offercat.cache.extra.GetMulFunction;
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
        CacheObject cacheObject = this.get(key, availableCache);
        if (cacheObject != null) {
            return availableCache.transferToObject(cacheObject);
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
        CacheObject cacheObject = this.get(key, availableCache);
        if (cacheObject != null) {
            T value = availableCache.transferToObject(cacheObject);
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

    private <T extends Serializable> CacheObject get(String key, AbstractCache node) {
        if (node == null) {
            return null;
        }
        CacheObject cacheObject = node.getCacheObject(key);
        if (cacheObject != null) {
            if (properties.isLogEnable()) {
                log.info("get | {} got the object, key = {}", node.getName(), key);
            }
            return cacheObject;
        }
        AbstractCache nextAvailableCache = this.getAvailableCache(node.getNext());
        CacheObject nextObject = this.get(key, nextAvailableCache);
        if (nextObject != null) {
            T obj = nextAvailableCache.transferToObject(nextObject);
            CacheObject thisObject = node.transferToCacheObject(obj, nextObject.getSetTime());
            node.setWithBroadcast(key, thisObject);
            return thisObject;
        }
        return null;
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
        Map<String, CacheObject> cacheObjectMap = this.getMul(keys, availableCache);
        if (keys.size() == 0 || getMulFunction == null) {
            return this.transferToObjectMap(cacheObjectMap, availableCache);
        }
        resultMap.putAll(this.transferToObjectMap(cacheObjectMap, availableCache));

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

    private Map<String, CacheObject> getMul(List<String> keys, AbstractCache node) {
        if (node == null) {
            return new HashMap<>(0);
        }
        Map<String, CacheObject> resultMap = node.getMulCacheObject(keys);
        resultMap = resultMap == null ? new HashMap<>(keys.size(), 2) : resultMap;
        if (properties.isLogEnable() && resultMap.size() > 0) {
            log.info("getMul | {} got {} objects", node.getName(), resultMap.size());
        }
        keys.removeAll(resultMap.keySet());
        if (keys.size() == 0) {
            return resultMap;
        }
        AbstractCache nextAvailableCache = this.getAvailableCache(node.getNext());
        Map<String, CacheObject> nextResultMap = getMul(keys, nextAvailableCache);
        // 回填
        if (!CollectionUtils.isEmpty(nextResultMap)) {
            Map<String, CacheObject> thisCacheObjectMap = this.backfillCacheObjectMap(nextResultMap, node, nextAvailableCache);
            resultMap.putAll(thisCacheObjectMap);
        }
        return resultMap;
    }

    private <T extends CacheId<V>, V> Map<String, CacheObject> backfillCacheObjectMap(Map<String, CacheObject> nextResultMap,
                                                                 AbstractCache thisCache,
                                                                 AbstractCache nextCache) {
        if (CollectionUtils.isEmpty(nextResultMap)) {
            return new HashMap<>(0);
        }
        Map<String, CacheObject> thisCacheObjectMap = new HashMap<>(nextResultMap.size(), 2);
        nextResultMap.forEach((key, nextObject) -> {
            T object = nextCache.transferToObject(nextObject);
            CacheObject thisObject = thisCache.transferToCacheObject(object, nextObject.getSetTime());
            thisCacheObjectMap.put(key, thisObject);
        });
        thisCache.setMulWithBroadcast(thisCacheObjectMap);
        return thisCacheObjectMap;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        this.set(key, value, beginNode);
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> keyValues) {
        this.setMul(keyValues, beginNode);
    }

    private <T extends Serializable> void setMul(Map<String, T> keyValues, AbstractCache node) {
        if (CollectionUtils.isEmpty(keyValues) || node == null) {
            return;
        }
        if (node.getItemProperties().isEnable()) {
            node.setMulWithBroadcast(transferToCacheObjectMap(keyValues, node, System.currentTimeMillis()));
        }
        this.setMul(keyValues, node.getNext());
    }

    private <T extends Serializable> void set(String key, T value, AbstractCache node) {
        if (StringUtils.isEmpty(key) || value == null || node == null) {
            return;
        }
        if (node.getItemProperties().isEnable()) {
            CacheObject cacheObject = node.transferToCacheObject(value, System.currentTimeMillis());
            node.setWithBroadcast(key, cacheObject);
        }
        this.set(key, value, node.getNext());
    }

    private <T extends Serializable> Map<String, CacheObject> transferToCacheObjectMap(Map<String, T> keyValues,
                                                                                        AbstractCache cache,
                                                                                        long time) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyMap();
        }
        Map<String, CacheObject> cacheObjectMap = new HashMap<>(keyValues.size(), 2);
        keyValues.forEach((key, object) ->
            cacheObjectMap.put(key, cache.transferToCacheObject(object, time))
        );
        return cacheObjectMap;
    }

    private <T extends Serializable> Map<String, T> transferToObjectMap(Map<String, CacheObject> cacheObjectMap,
                                                                        AbstractCache cache) {
        if (CollectionUtils.isEmpty(cacheObjectMap)) {
            return Collections.emptyMap();
        }
        Map<String, T> objectMap = new HashMap<>(cacheObjectMap.size(), 2);
        cacheObjectMap.forEach((key, cacheObject) ->
                objectMap.put(key, cache.transferToObject(cacheObject))
        );
        return objectMap;
    }
}
