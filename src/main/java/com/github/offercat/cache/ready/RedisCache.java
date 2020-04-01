package com.github.offercat.cache.ready;

import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.extra.CacheEntity;
import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.Serializer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 开箱即用的 Redis 集群缓存
 * Out of the box Redis cluster cache
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 15:06:45
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor
public class RedisCache extends ClusterCache {

    private JedisPool jedisPool;
    private ExecutorService asyncPool;
    private Serializer serializer;

    public RedisCache(String name, Serializer serializer, ItemProperties itemProperties) {
        super(name, itemProperties);
        this.serializer = serializer;
    }

    @Override
    public void initMiddleware(ItemProperties itemProperties) {
        ExceptionUtil.paramPositive(itemProperties.getTimeout(), "Expiration time must be greater than 0!");
        this.jedisPool = MiddlewareCreator.createJedisPool(itemProperties);
        this.asyncPool = new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(Integer.MAX_VALUE),
                new ThreadFactoryBuilder().setNameFormat("RedisCache Pool-%d").setDaemon(false).build(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Override
    public boolean supportBroadcast() {
        return false;
    }

    @Override
    public <T extends Serializable> T get(String key) {
        CacheEntity cacheEntity = this.getCacheEntity(key);
        if (cacheEntity == null) {
            return null;
        }
        return this.getCacheEntityParser().toObject(cacheEntity);
    }

    @Override
    public <T extends Serializable> Map<String, T> getMul(List<String> keys) {
        Map<String, T> result = new HashMap<>(keys.size(), 2);
        if (StringUtils.isEmpty(keys)) {
            return result;
        }
        List<String> strObjList = jedisPool.getResource().mget(keys.toArray(new String[0]));
        Class<T> type = null;
        for (int i = 0; i < strObjList.size(); i++) {
            if (strObjList.get(i) != null) {
                CacheEntity cacheEntity = this.serializer.deserializeFromString(strObjList.get(i), CacheEntity.class);
                if (type == null) {
                    try {
                        type = (Class<T>) Class.forName(cacheEntity.getTypeStr());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                result.put(keys.get(i), this.serializer.deserializeFromString((String) cacheEntity.getObject(), type));
            }
        }
        return result;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        String strCacheEntity = this.serializer.serializeToString(value);
        CacheEntity cacheEntity = this.getCacheEntityParser().toCacheEntity(strCacheEntity, System.currentTimeMillis());
        this.setCacheEntity(key, cacheEntity);
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        String[] keyValues = new String[keyObjects.size() * 2];
        int i = 0;
        for (Map.Entry<String, T> entry : keyObjects.entrySet()) {
            if (StringUtils.isEmpty(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            CacheEntity cacheEntity = this.getCacheEntityParser().toCacheEntity(
                    this.serializer.serializeToString(entry.getValue()),
                    System.currentTimeMillis()
            );
            keyValues[i] = entry.getKey();
            keyValues[i + 1] = this.serializer.serializeToString(cacheEntity);
            i = i + 2;
        }
        jedisPool.getResource().mset(keyValues);
        int expireSeconds = this.getTimeUnitToMillisecond();
        asyncPool.execute(() -> keyObjects.keySet().forEach(key -> jedisPool.getResource().expire(key, expireSeconds)));
    }

    @Override
    public void del(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        jedisPool.getResource().del(key);
    }

    @Override
    public void delMul(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        jedisPool.getResource().del(keys.toArray(new String[0]));
    }

    @Override
    public void setCacheEntity(String key, CacheEntity cacheEntity) {
        if (StringUtils.isEmpty(key) || cacheEntity == null) {
            return;
        }
        String cacheObjectStr = this.serializer.serializeToString(cacheEntity);
        jedisPool.getResource().setex(key, this.getTimeUnitToMillisecond(), cacheObjectStr);
    }

    @Override
    public void setMulCacheEntity(Map<String, CacheEntity> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        String[] keyValues = new String[keyObjects.size() * 2];
        int i = 0;
        for (Map.Entry<String, CacheEntity> entry : keyObjects.entrySet()) {
            if (StringUtils.isEmpty(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            keyValues[i] = entry.getKey();
            keyValues[i + 1] = this.serializer.serializeToString(entry.getValue());
            i = i + 2;
        }
        jedisPool.getResource().mset(keyValues);
        int expireSeconds = this.getTimeUnitToMillisecond();
        asyncPool.execute(() -> keyObjects.keySet().forEach(key -> jedisPool.getResource().expire(key, expireSeconds)));
    }

    @Override
    public CacheEntity getCacheEntity(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String cacheObjectStr = jedisPool.getResource().get(key);
        if (StringUtils.isEmpty(cacheObjectStr)) {
            return null;
        }
        return this.serializer.deserializeFromString(cacheObjectStr, CacheEntity.class);
    }

    @Override
    public Map<String, CacheEntity> getMulCacheEntity(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyMap();
        }
        Map<String, CacheEntity> cacheObjectMap = new HashMap<>(keys.size(), 2);
        List<String> strObjList = jedisPool.getResource().mget(keys.toArray(new String[0]));
        for (int i = 0; i < strObjList.size(); i++) {
            if (strObjList.get(i) != null) {
                CacheEntity cacheEntity = this.serializer.deserializeFromString(strObjList.get(i), CacheEntity.class);
                cacheObjectMap.put(keys.get(i), cacheEntity);
            }
        }
        return cacheObjectMap;
    }

    private int getTimeUnitToMillisecond() {
        long result = -1;
        long timeout = this.getItemProperties().getTimeout();
        TimeUnit timeUnit = this.getItemProperties().getTimeunit();
        if (timeUnit == TimeUnit.SECONDS) {
            result = timeout;
        } else if (timeUnit == TimeUnit.MINUTES) {
            result = timeout * 60;
        } else if (timeUnit == TimeUnit.HOURS) {
            result = timeout * 60 * 60;
        } else if (timeUnit == TimeUnit.DAYS) {
            result = timeout * 60 * 60 * 24;
        }
        if (result == -1) {
            throw new InvalidParameterException("The minimum unit of Redis cache is seconds!");
        }
        if (result > Integer.MAX_VALUE) {
            throw new InvalidParameterException("Redis cache expiration time exceeds the maximum range!");
        }
        return (int) result;
    }

    @Override
    public <T> Map<String, T> getMap(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        Map<String, String> strMap =  jedisPool.getResource().hgetAll(key);

        return null;
    }

    @Override
    public <T> void setMap(String key, Map<String, T> map) {

    }

    @Override
    public <T> T getMapField(String key, String field) {
        return null;
    }

    @Override
    public <T> void setMapField(String key, String field, T value) {

    }

    @Override
    public void delMapField(String key, String field) {

    }

    @Override
    public boolean mapExistField(String key, String field) {
        return false;
    }
}
