package com.github.offercat.cache.ready;

import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.inte.DirectCache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 开箱即用的 EhCache 直接缓存
 * Out of the box EhCache direct cache
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 17:15:05
 */
public class EhDirectCache extends DirectCache {

    public EhDirectCache(String name, CacheProperties cacheProperties) {
        super(name, cacheProperties);
    }

    @Override
    public <T extends Serializable> T get(String key) {
        return null;
    }

    @Override
    public <T extends Serializable> Map<String, T> getMul(List<String> keys) {
        return null;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {

    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> map) {

    }

    @Override
    public void del(String key) {

    }

    @Override
    public void delMul(List<String> keys) {

    }
}
