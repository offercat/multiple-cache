package com.github.offercat.cache.extra;

import java.util.Collection;

/**
 * 带 key list 的function
 *
 * @author 徐通
 * @since 2019年9月22日02:15:33
 */
@FunctionalInterface
public interface GetMulFunction<T, V> {

    /**
     * 获取多个键值对
     *
     * @param objectIdList 对象主键 list
     * @return 键值对
     */
    Collection<T> get(Collection<V> objectIdList);
}
