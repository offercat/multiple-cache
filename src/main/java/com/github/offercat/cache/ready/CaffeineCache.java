package com.github.offercat.cache.ready;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.extra.CacheObject;
import com.github.offercat.cache.inte.LocalCache;
import com.github.offercat.cache.inte.Serializer;
import com.github.offercat.cache.proxy.UnIntercept;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开箱即用的 Caffeine 本地缓存
 * Out of the box Caffeine local cache
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 15:05:35
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor
public class CaffeineCache extends LocalCache {

    private Cache<String, Object> caffeine;

    public CaffeineCache(String name, ItemProperties itemProperties) {
        super(name, itemProperties);
    }

    @Override
    public void initMiddleware(ItemProperties itemProperties) {
        ExceptionUtil.paramPositive(itemProperties.getMaxSize(), "Max size must be greater than 0!");
        ExceptionUtil.paramPositive(itemProperties.getTimeout(), "Expiration time must be greater than 0!");
        if (this.caffeine == null) {
            this.caffeine = MiddlewareCreator.createCaffeine(itemProperties);
        }
    }

    @Override
    public boolean supportBroadcast() {
        return true;
    }

    @Override
    public <T extends Serializable> T get(String key) {
        CacheObject cacheObject = this.getCacheObject(key);
        return cacheObject == null ? null : (T) cacheObject.getObject();
    }

    @Override
    public <T extends Serializable> Map<String, T> getMul(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>(0);
        }
        Map<String, Object> cacheResult = this.caffeine.getAllPresent(keys);
        Map<String, T> result = new HashMap<>(cacheResult.size(), 2);
        cacheResult.forEach((key, obj) -> {
            if (obj instanceof CacheObject) {
                CacheObject cacheObject = (CacheObject) obj;
                result.put(key, (T) cacheObject.getObject());
            }
        });
        return result;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        if (key == null || value == null) {
            return;
        }
        CacheObject cacheObject = new CacheObject(
                ((Object) value).getClass().getName(),
                value,
                System.currentTimeMillis()
        );
        this.setCacheObject(key, cacheObject);
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        long time = System.currentTimeMillis();
        Map<String, CacheObject> objectMap = new HashMap<>(keyObjects.size(), 2);
        keyObjects.forEach((key, value) -> {
            CacheObject cacheObject = new CacheObject(
                    ((Object) value).getClass().getName(),
                    value,
                    time
            );
            objectMap.put(key, cacheObject);
        });
        this.setMulCacheObject(objectMap);
    }

    @Override
    public void del(String key) {
        if (key == null) {
            return;
        }
        this.caffeine.invalidate(key);
    }

    @Override
    public void delMul(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        this.caffeine.invalidateAll(keys);
    }

    @Override
    public void setCacheObject(String key, CacheObject cacheObject) {
        if (StringUtils.isEmpty(key) || cacheObject == null) {
            return;
        }
        this.caffeine.put(key, cacheObject);
    }

    @Override
    public void setMulCacheObject(Map<String, CacheObject> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        this.caffeine.putAll(keyObjects);
    }

    @Override
    public CacheObject getCacheObject(String key) {
        if (key == null) {
            return null;
        }
        return (CacheObject) caffeine.getIfPresent(key);
    }

    @Override
    public Map<String, CacheObject> getMulCacheObject(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>(0);
        }
        Map<String, Object> cacheResult = this.caffeine.getAllPresent(keys);
        Map<String, CacheObject> result = new HashMap<>(cacheResult.size(), 2);
        cacheResult.forEach((key, obj) -> {
            if (obj instanceof CacheObject) {
                result.put(key, (CacheObject) obj);
            }
        });
        return result;
    }

    @Override
    public <T extends Serializable> T transfer(CacheObject cacheObject) {
        return null;
    }

    @Override
    public <T extends Serializable> CacheObject transfer(T obj, long time) {
        return null;
    }
}
