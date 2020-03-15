package com.github.offercat.cache.inte;

import com.github.offercat.cache.config.ItemProperties;

/**
 * 抽象本地缓存，所有本地缓存继承这个抽象类
 * Abstract local cache. All local caches inherit this abstract class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 16:33:50
 */
public abstract class LocalCache extends AbstractCache {

    public LocalCache(String name, Serializer serializer, ItemProperties itemProperties){
        super(name, serializer, itemProperties);
    }
}
