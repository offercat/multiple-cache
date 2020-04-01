package com.github.offercat.cache.extra;

import java.io.Serializable;

/**
 * 不需要序列化的缓存实体解析器
 *
 * @author 徐通 xutong34
 * @since 2020年03月29日 18:17:36
 */
@SuppressWarnings("unchecked")
public class UnSerializeEntityParser extends AbstractCacheEntityParser {

    @Override
    public <T extends Serializable> T toObject(CacheEntity cacheEntity) {
        if (cacheEntity == null || cacheEntity.getObject() == null) {
            return null;
        }
        return (T) cacheEntity.getObject();
    }

    @Override
    public <T extends Serializable> CacheEntity toCacheEntity(T obj, long time) {
        if (obj == null) {
            return null;
        }
        return new CacheEntity(obj.getClass().getName(), obj, time);
    }
}
