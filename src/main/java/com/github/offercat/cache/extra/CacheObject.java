package com.github.offercat.cache.extra;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 内置的缓存对象，这个对象会记录序列化对象的 Class 类型，设置时间戳，在广播时会对比时间戳，防止缓存错乱
 * Built in cache object. This object will record the class type of the serialized object,
 * set the time stamp, and compare the time stamp when broadcasting to prevent the cache from being disordered
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020/3/3 10:55
 */
@Data
@AllArgsConstructor
public class CacheObject implements Serializable {

    private static final long serialVersionUID = 721720413226228880L;

    /**
     * 缓存Class类型
     * Object class type
     */
    private String typeStr;

    /**
     * 对象序列化后的字符串
     * String after object serialization
     */
    private Object object;

    /**
     * 对象写入缓存的时间
     * Time to write to cache
     */
    private long setTime;
}
