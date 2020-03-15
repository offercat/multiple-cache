package com.github.offercat.cache.ready;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.exception.ExceptionUtil;
import com.github.offercat.cache.inte.LocalCache;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 咖啡因本地缓存
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 15:05:35
 */
@SuppressWarnings("unchecked")
public class CaffeineCache extends LocalCache {

    private Cache<String, Object> caffeine;

    public CaffeineCache(String name, CacheProperties properties) {
        super(name, properties);
        ItemProperties itemProperties = this.getItemProperties();
        if (itemProperties.isEnable()) {
            ExceptionUtil.paramPositive(itemProperties.getMaxSize(), "最大容量必须大于0！");
            ExceptionUtil.paramPositive(itemProperties.getTimeout(), "过期时间必须大于0！");
            this.caffeine = MiddlewareCreator.createCaffeine(itemProperties);
        }
    }

    @Override
    public <T extends Serializable> T get(String key) {
        if (key == null) {
            return null;
        }
        return (T) caffeine.getIfPresent(key);
    }

    @Override
    public <T extends Serializable> Map<String, T> getMul(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return new HashMap<>(0);
        }
        Map<String, Object> cacheResult = this.caffeine.getAllPresent(keys);
        Map<String, T> result = new HashMap<>(cacheResult.size(), 2);
        cacheResult.forEach((key, obj) -> {
            if (obj instanceof Serializable) {
                result.put(key, (T) obj);
            }
        });
        return result;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        if (key == null || value == null) {
            return;
        }
        this.caffeine.put(key, value);
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> map) {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        this.caffeine.putAll(map);
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
}
