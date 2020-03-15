package com.github.offercat.cache.inte;

import com.github.offercat.cache.config.CacheProperties;

/**
 * Description
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 17:06:06
 */
public abstract class DirectCache extends AbstractCache {

    public DirectCache(String name, CacheProperties cacheProperties) {
        super(name, cacheProperties);
    }
}
