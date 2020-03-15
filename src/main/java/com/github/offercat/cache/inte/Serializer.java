package com.github.offercat.cache.inte;

import java.io.Serializable;

/**
 * 序列化器接口
 * Serializer interface
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 17:45:43
 */
public interface Serializer {

    /**
     * 将对象序列化成字节数组
     * Sequence object into byte arrays
     *
     * @param object object
     * @return byte arrays
     */
    <T extends Serializable> byte[] serializeToBytes(T object);

    /**
     * 将字节数组反序列化成对象
     * Deserialize byte array to object
     *
     * @param bytes byte arrays
     * @return object
     */
    <T extends Serializable> T deserializeFromBytes(byte[] bytes);

    /**
     * 将对象序列化成字符串
     * Sequence object into string
     *
     * @param object object
     * @return string
     */
    <T extends Serializable> String serializeToString(T object);

    /**
     * 将字符串反序列化为对象
     * Deserialize string to object
     *
     * @param str  string
     * @param type object class type
     * @return object
     */
    <T extends Serializable> T deserializeFromString(String str, Class<T> type);
}
