package com.github.offercat.cache.extra;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 缓存对象
 *
 * @author 徐通 xutong34
 * @since 2020/3/3 10:55
 */
@Data
@AllArgsConstructor
public class CacheObject implements Serializable {

    private static final long serialVersionUID = 721720413226228880L;

    private String typeStr;

    private String serializeStr;

    private long setTime;
}
