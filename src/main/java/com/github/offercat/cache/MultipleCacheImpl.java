package com.github.offercat.cache;

import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.extra.CacheId;
import com.github.offercat.cache.extra.CacheKeyGenerate;
import com.github.offercat.cache.extra.GetMulFunction;
import com.github.offercat.cache.inte.AbstractCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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
        T value = this.get(key, beginNode);
        if (value != null) {
            return value;
        }
        if (callback == null) {
            return null;
        }
        T callbackValue = callback.get();
        if (callbackValue != null) {
            if (properties.isLogEnable()) {
                log.info("get | Callback function got the object, key = {}", key);
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
        T value = this.get(key, beginNode);
        if (value != null) {
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
                log.info("get | Callback function got the object, key = {}", key);
            }
            this.set(key, callbackValue);
            return callbackValue;
        }
        if (properties.isLogEnable()) {
            log.info("getMul | Backfill null value {}", nullValue);
        }
        this.set(key, nullValue);
        return null;
    }

    private <T extends Serializable> T get(String key, AbstractCache node) {
        if (node == null) {
            return null;
        }
        if (!node.getItemProperties().isEnable()) {
            return get(key, node.getNext());
        }
        T value = node.get(key);
        if (value != null) {
            if (properties.isLogEnable()) {
                log.info("get | {} got the object, key = {}", node.getName(), key);
            }
            return value;
        }
        T nextValue = get(key, node.getNext());
        if (nextValue != null) {
            node.set(key, nextValue);
        }
        return nextValue;
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

    public <T extends CacheId<V>, V> Map<String, T> getMul(Collection<V> objectIds,
                                                          CacheKeyGenerate<V> cacheKeyGenerate,
                                                          GetMulFunction<T, V> getMulFunction,
                                                           boolean fillBack, T nullValue) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return new HashMap<>();
        }
        Map<String, V> keyIdMap = new HashMap<>(objectIds.size(), 2);
        objectIds.forEach(objectId -> keyIdMap.put(cacheKeyGenerate.get(objectId), objectId));

        List<String> keys = new ArrayList<>(keyIdMap.keySet());
        Map<String, T> resultMap = this.getMul(keys, beginNode);
        if (keys.size() == 0 || getMulFunction == null) {
            return resultMap;
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
                log.info("getMul | Backfill null value, size = {}", keys.size());
            }
            Map<String, T> nullValueMap = new HashMap<>(keys.size(), 2);
            keys.forEach(key -> nullValueMap.put(key, nullValue));
            this.setMul(nullValueMap);
        }
        return resultMap;
    }

    private <T extends CacheId<V>, V> Map<String, T> getMul(List<String> keys, AbstractCache node) {
        if (node == null) {
            return new HashMap<>(0);
        }
        if (!node.getItemProperties().isEnable()) {
            return getMul(keys, node.getNext());
        }
        Map<String, T> resultMap = node.getMul(keys);
        if (properties.isLogEnable() && resultMap.size() > 0) {
            log.info("getMul | {} got {} objects", node.getName(), resultMap.size());
        }
        keys.removeAll(resultMap.keySet());
        if (keys.size() == 0) {
            return resultMap;
        }
        Map<String, T> nextResultMap = getMul(keys, node.getNext());
        // 回填
        if (!CollectionUtils.isEmpty(nextResultMap)) {
            node.setMul(nextResultMap);
        }
        resultMap.putAll(nextResultMap);
        return resultMap;
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
        if (node == null) {
            return;
        }
        if (node.getItemProperties().isEnable()) {
            node.setMul(keyValues);
        }
        this.setMul(keyValues, node.getNext());
    }

    private <T extends Serializable> void set(String key, T value, AbstractCache node) {
        if (node == null) {
            return;
        }
        if (node.getItemProperties().isEnable()) {
            node.set(key, value);
        }
        this.set(key, value, node.getNext());
    }
}
