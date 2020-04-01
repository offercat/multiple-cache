package com.github.offercat.cache.extra;

import com.github.offercat.cache.inte.Serializer;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 字符串序列化缓存实体解析器
 *
 * @author 徐通 xutong34
 * @since 2020年03月29日 18:10:35
 */
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class SerializeStringEntityParser extends AbstractCacheEntityParser {

    private Serializer serializer;

    @Override
    public <T extends Serializable> T toObject(CacheEntity cacheEntity) {
        if (cacheEntityIsUnavailable(cacheEntity)) {
            return null;
        }
        String objStr = (String) cacheEntity.getObject();
        Class<T> type;
        try {
            type = (Class<T>) Class.forName(cacheEntity.getTypeStr());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return serializer.deserializeFromString(objStr, type);
    }

    @Override
    public <T extends Serializable> CacheEntity toCacheEntity(T obj, long time) {
        if (obj == null) {
            return null;
        }
        return new CacheEntity(obj.getClass().getName(), serializer.serializeToString(obj), time);
    }
}
