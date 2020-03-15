package com.github.offercat.cache.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存接口，通用缓存接口
 *
 * @author 徐通
 * @since 2019年12月08日 2:57
 */
public interface BaseAction {

    /**
     * 获取对象
     *
     * @param key key
     * @return 对象
     */
    <T extends Serializable> T get(String key);

    /**
     * 批量获取对象
     *
     * @param keys 缓存key集合
     * @return 键值对
     */
    <T extends Serializable> Map<String, T> getMul(List<String> keys);

    /**
     * 存储对象
     *
     * @param key 缓存key
     * @param value 对象
     */
    <T extends Serializable> void set(String key, T value);

    /**
     * 设置多个对象
     *
     * @param map 键值对
     */
    <T extends Serializable> void setMul(Map<String, T> map);

    /**
     * 删除对象
     *
     * @param key 缓存key
     */
    void del(String key);

    /**
     * 批量删除
     *
     * @param keys 缓存key集合
     */
    void delMul(List<String> keys);
}
