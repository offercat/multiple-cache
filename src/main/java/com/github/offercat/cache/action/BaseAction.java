package com.github.offercat.cache.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存接口，通用缓存接口
 * Basic cache interface, all subclasses need to implement it
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019年12月08日 2:57
 */
public interface BaseAction {

    /**
     * 获取对象
     * Get object
     *
     * @param key key
     * @return object
     */
    <T extends Serializable> T get(String key);

    /**
     * 批量获取对象
     * Get multiple objects
     *
     * @param keys key list
     * @return multiple objects
     */
    <T extends Serializable> Map<String, T> getMul(List<String> keys);

    /**
     * 存储对象
     * Save object
     *
     * @param key   key
     * @param value object
     */
    <T extends Serializable> void set(String key, T value);

    /**
     * 设置多个对象
     * Save multiple objects
     *
     * @param map key-value map
     */
    <T extends Serializable> void setMul(Map<String, T> map);

    /**
     * 删除对象
     * Delete object
     *
     * @param key key
     */
    void del(String key);

    /**
     * 批量删除
     * Delete multiple objects
     *
     * @param keys key list
     */
    void delMul(List<String> keys);
}
