package com.github.offercat.cache.extra;

import java.util.Collection;

/**
 * 批量回调策略
 * Batch callback policy
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019年9月22日02:15:33
 */
@FunctionalInterface
public interface GetMulFunction<T, V> {

    /**
     * 给定对象唯一标识的集合，返回对应的对象集合
     * Given the collection of unique ID of the object, return the corresponding object collection
     *
     * @param objectIds collection of unique ID
     * @return corresponding object collection
     */
    Collection<T> get(Collection<V> objectIds);
}
