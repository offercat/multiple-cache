package com.github.offercat.cache.ready;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.extra.CacheEntity;
import com.github.offercat.cache.inte.LocalCache;
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
        CacheEntity cacheEntity = this.getCacheEntity(key);
        return cacheEntity == null ? null : this.getCacheEntityParser().toObject(cacheEntity);
    }

    @Override
    public <T extends Serializable> Map<String, T> getMul(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>(0);
        }
        Map<String, Object> cacheResult = this.caffeine.getAllPresent(keys);
        Map<String, T> result = new HashMap<>(cacheResult.size(), 2);
        cacheResult.forEach((key, obj) -> {
            if (obj instanceof CacheEntity) {
                CacheEntity cacheEntity = (CacheEntity) obj;
                result.put(key, this.getCacheEntityParser().toObject(cacheEntity));
            }
        });
        return result;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        if (key == null || value == null) {
            return;
        }
        CacheEntity cacheEntity = this.getCacheEntityParser().toCacheEntity(value, System.currentTimeMillis());
        this.setCacheEntity(key, cacheEntity);
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        long time = System.currentTimeMillis();
        Map<String, CacheEntity> objectMap = this.getCacheEntityParser().toCacheEntityMap(keyObjects, time);
        this.setMulCacheEntity(objectMap);
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
    public void setCacheEntity(String key, CacheEntity cacheEntity) {
        if (StringUtils.isEmpty(key) || cacheEntity == null) {
            return;
        }
        this.caffeine.put(key, cacheEntity);
    }

    @Override
    public void setMulCacheEntity(Map<String, CacheEntity> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        this.caffeine.putAll(keyObjects);
    }

    @Override
    public CacheEntity getCacheEntity(String key) {
        if (key == null) {
            return null;
        }
        return (CacheEntity) caffeine.getIfPresent(key);
    }

    @Override
    public Map<String, CacheEntity> getMulCacheEntity(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>(0);
        }
        Map<String, Object> cacheResult = this.caffeine.getAllPresent(keys);
        Map<String, CacheEntity> result = new HashMap<>(cacheResult.size(), 2);
        cacheResult.forEach((key, obj) -> {
            if (obj instanceof CacheEntity) {
                result.put(key, (CacheEntity) obj);
            }
        });
        return result;
    }
}
