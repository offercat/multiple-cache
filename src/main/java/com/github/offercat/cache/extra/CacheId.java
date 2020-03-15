package com.github.offercat.cache.extra;

import java.io.Serializable;

/**
 * 带唯一标识的对象，使用批量获取方法的对象必须实现这个接口，并且返回对象的唯一标识
 * For the object with unique ID, the object using batch get method must implement this interface and return the unique ID of the object
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019/10/10 15:29
 */
public interface CacheId<V> extends Serializable {

    /**
     * 获取对象的唯一标识，这个标识根据用户定义的缓存Key生成策略生成缓存Key
     * Get the unique ID of the object, which generates the cache key according to the user-defined cache key generation policy
     *
     * @return unique ID
     */
    V getObjectId();
}
