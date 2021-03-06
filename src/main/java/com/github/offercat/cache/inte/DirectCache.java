package com.github.offercat.cache.inte;

import com.github.offercat.cache.config.ItemProperties;
import lombok.NoArgsConstructor;

/**
 * 抽象直接缓存，所有直接缓存继承这个抽象类
 * Abstract direct cache. All direct caches inherit this abstract class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 17:06:06
 */
@NoArgsConstructor
public abstract class DirectCache extends AbstractCache {

    public DirectCache(String name, ItemProperties itemProperties){
        super(name, itemProperties);
    }
}
