package com.github.offercat.cache.broadcast;

import com.github.offercat.cache.extra.CacheEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 广播对象，保存缓存 key 与 cacheObject 之间的关系
 * Broadcast object, save the relationship between cache key and cacheObject
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月15日 17:59:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastObject implements Serializable {

    /**
     * 缓存key
     * cache key
     */
    private String key;

    /**
     * 内置缓存对象
     * Built-in cache object
     */
    private CacheEntity cacheEntity;
}
