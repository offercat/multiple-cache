package com.github.offercat.cache.ready;

import com.alibaba.fastjson.JSON;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.extra.CacheObject;
import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.Serializer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;
import java.security.InvalidParameterException;
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
public class RedisCache extends ClusterCache {

    private JedisPool jedisPool;
    private ExecutorService asyncPool;

    public RedisCache(String name, Serializer serializer, ItemProperties itemProperties) {
        super(name, serializer, itemProperties);
        if (itemProperties.isEnable()) {
            ExceptionUtil.paramPositive(itemProperties.getTimeout(), "Expiration time must be greater than 0!");
            this.jedisPool = MiddlewareCreator.createJedisPool(itemProperties);
        }
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
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String strObj = jedisPool.getResource().get(key);
        if (strObj == null) {
            return null;
        }
        return this.parseCacheObject(strObj);
    }

    private <T extends Serializable> T parseCacheObject(String strObj) {
        CacheObject cacheObject = JSON.parseObject(strObj, CacheObject.class);
        Class<T> type;
        try {
            type = (Class<T>) Class.forName(cacheObject.getTypeStr());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this.getSerializer().deserializeFromString((String) cacheObject.getObject(), type);
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
                CacheObject cacheObject = JSON.parseObject(strObjList.get(i), CacheObject.class);
                if (type == null) {
                    try {
                        type = (Class<T>) Class.forName(cacheObject.getTypeStr());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                result.put(keys.get(i), this.getSerializer().deserializeFromString((String) cacheObject.getObject(), type));
            }
        }
        return result;
    }

    @Override
    public <T extends Serializable> void set(String key, T value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        CacheObject cacheObject = new CacheObject(
                ((Object) value).getClass().getName(),
                this.getSerializer().serializeToString(value),
                System.currentTimeMillis()
        );
        this.setCacheObject(key, cacheObject);
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
            CacheObject cacheObject = new CacheObject(
                    ((Object) entry.getValue()).getClass().getName(),
                    this.getSerializer().serializeToString(entry.getValue()),
                    System.currentTimeMillis()
            );
            keyValues[i] = entry.getKey();
            keyValues[i + 1] = JSON.toJSONString(cacheObject);
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
    public void setCacheObject(String key, CacheObject cacheObject) {
        if (StringUtils.isEmpty(key) || cacheObject == null) {
            return;
        }
        jedisPool.getResource().setex(key, this.getTimeUnitToMillisecond(), JSON.toJSONString(cacheObject));
    }

    @Override
    public void setMulCacheObject(Map<String, CacheObject> keyObjects) {
        if (CollectionUtils.isEmpty(keyObjects)) {
            return;
        }
        String[] keyValues = new String[keyObjects.size() * 2];
        int i = 0;
        for (Map.Entry<String, CacheObject> entry : keyObjects.entrySet()) {
            if (StringUtils.isEmpty(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            keyValues[i] = entry.getKey();
            keyValues[i + 1] = JSON.toJSONString(entry.getValue());
            i = i + 2;
        }
        jedisPool.getResource().mset(keyValues);
        int expireSeconds = this.getTimeUnitToMillisecond();
        asyncPool.execute(() -> keyObjects.keySet().forEach(key -> jedisPool.getResource().expire(key, expireSeconds)));
    }

    @Override
    public CacheObject getCacheObject(String key) {
        return null;
    }

    @Override
    public Map<String, CacheObject> getMulCacheObject(List<String> keys) {
        return null;
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
}
