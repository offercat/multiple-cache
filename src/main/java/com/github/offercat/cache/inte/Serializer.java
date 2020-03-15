package com.github.offercat.cache.inte;

import java.io.Serializable;

/**
 * 序列化器
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 17:45:43
 */
public interface Serializer {

    /**
     * 将对象序列化成字节数组
     *
     * @param object 对象
     * @return 字节数组
     */
    <T extends Serializable> byte[] serializeToBytes(T object);

    /**
     * 将字节数组反序列化成对象
     *
     * @param bytes 字节数组
     * @return 对象
     */
    <T extends Serializable> T deserializeFromBytes(byte[] bytes);

    /**
     * 将对象序列化成字符串
     *
     * @param object 对象
     * @return 字符串
     */
    <T extends Serializable> String serializeToString(T object);

    /**
     * 将对象序列化成字符串
     *
     * @param str 字符串
     * @param type 对象类型
     * @return 对象
     */
    <T extends Serializable> T deserializeFromString(String str, Class<T> type);
}
