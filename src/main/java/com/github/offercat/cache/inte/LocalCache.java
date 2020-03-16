package com.github.offercat.cache.inte;

import com.github.offercat.cache.config.ItemProperties;
import lombok.NoArgsConstructor;

/**
 * 抽象本地缓存，所有本地缓存继承这个抽象类
 * Abstract local cache. All local caches inherit this abstract class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 16:33:50
 */
@NoArgsConstructor
public abstract class LocalCache extends AbstractCache {

    public LocalCache(String name, ItemProperties itemProperties){
        super(name, itemProperties);
    }
}
