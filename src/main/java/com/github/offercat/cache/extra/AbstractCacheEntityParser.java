package com.github.offercat.cache.extra;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象的缓存对象解析器
 *
 * @author 徐通 xutong34
 * @since 2020年03月29日 18:43:51
 */
public abstract class AbstractCacheEntityParser implements CacheEntityParser {

    @Override
    public <T extends Serializable> CacheEntity transferCacheEntity(CacheEntity originEntity, CacheEntityParser originParser) {
        if (this.equals(originParser)) {
            return originEntity;
        } else {
            T originObject = originParser.toObject(originEntity);
            return this.toCacheEntity(originObject, originEntity.getTime());
        }
    }

    @Override
    public <T extends Serializable> Map<String, CacheEntity> transferCacheEntityMap(Map<String, CacheEntity> originEntityMap,
                                                                                    CacheEntityParser originParser) {
        if (this.equals(originParser)) {
            return originEntityMap;
        } else {
            Map<String, CacheEntity> thisCacheEntityMap = new HashMap<>(originEntityMap.size(), 2);
            originEntityMap.forEach((key, originEntity) -> {
                if (cacheEntityIsUnavailable(originEntity)) {
                    return;
                }
                T originObject = originParser.toObject(originEntity);
                thisCacheEntityMap.put(key, this.toCacheEntity(originObject, originEntity.getTime()));
            });
            return thisCacheEntityMap;
        }
    }

    @Override
    public <T extends Serializable> Map<String, T> toObjectMap(Map<String, CacheEntity> cacheEntityMap) {
        if (CollectionUtils.isEmpty(cacheEntityMap)) {
            return Collections.emptyMap();
        }
        Map<String, T> objectMap = new HashMap<>(cacheEntityMap.size(), 2);
        cacheEntityMap.forEach((key, cacheEntity) -> {
            if (cacheEntityIsUnavailable(cacheEntity)) {
                return;
            }
            objectMap.put(key, this.toObject(cacheEntity));
        });
        return objectMap;
    }

    @Override
    public <T extends Serializable> Map<String, CacheEntity> toCacheEntityMap(Map<String, T> keyValues, long time) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyMap();
        }
        Map<String, CacheEntity> cacheObjectMap = new HashMap<>(keyValues.size(), 2);
        keyValues.forEach((key, object) ->
                cacheObjectMap.put(key, this.toCacheEntity(object, time))
        );
        return cacheObjectMap;
    }

    protected boolean cacheEntityIsUnavailable(CacheEntity cacheEntity) {
        return cacheEntity == null || cacheEntity.getObject() == null || StringUtils.isEmpty(cacheEntity.getTypeStr());
    }
}
