package com.github.offercat.cache.inte;

import com.github.offercat.cache.config.CacheProperties;

/**
 * Description
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 16:33:50
 */
public abstract class LocalCache extends AbstractCache {

    public LocalCache(String name, CacheProperties cacheProperties) {
        super(name, cacheProperties);
    }
}
