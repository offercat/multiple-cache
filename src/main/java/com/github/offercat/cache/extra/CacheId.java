package com.github.offercat.cache.extra;

import java.io.Serializable;

/**
 * 带主键的对象，获取缓存对象时会用到
 *
 * @author 徐通 xutong34
 * @since 2019/10/10 15:29
 */
public interface CacheId<V> extends Serializable {

    /**
     * 获取对象 id
     *
     * @return 对象 id
     */
    V getObjectId();
}
