package com.github.offercat.cache.action;

import java.util.Map;

/**
 * map 相关操作
 * map related operations
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 14:24:39
 */
public interface MapAction {

    /**
     * 获取映射集
     * Get map
     *
     * @param key cache key
     * @return map
     */
    <T> Map<String, T> getMap(String key);

    /**
     * 设置映射集，过期时间为配置的默认时间
     * Set up full map，expiration time is the default time configured
     *
     * @param key key
     * @param map 映射集
     */
    <T> void setMap(String key, Map<String, T> map);

    /**
     * 获取 map 的字段值
     * Get the field value of map
     *
     * @param key cache key
     * @param field field name
     * @return value of the field
     */
    <T> T getMapField(String key, String field);

    /**
     * 设置映射集字段
     *
     * @param key cache key
     * @param field field name
     * @param value field value
     */
    <T> void setMapField(String key, String field, T value);

    /**
     * 删除映射集字段
     *
     * @param key cache key
     * @param field field name
     */
    void delMapField(String key, String field);

    /**
     * 判断映射集是否含有指定字段
     *
     * @param key key
     * @param field field
     * @return 映射集是否含有指定字段
     */
    boolean mapExistField(String key, String field);
}
