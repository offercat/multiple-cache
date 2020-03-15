package com.github.offercat.cache.ready;

import com.alibaba.fastjson.JSON;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.exception.ExceptionUtil;
import com.github.offercat.cache.extra.CacheObject;
import com.github.offercat.cache.inte.ClusterCache;
import com.github.offercat.cache.inte.Serializer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * redis缓存
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 15:06:45
 */
@SuppressWarnings("unchecked")
public class RedisCache extends ClusterCache {

    private Jedis jedis;
    private Serializer serializer;
    private ExecutorService asyncPool;

    public RedisCache(String name, Serializer serializer, CacheProperties cacheProperties) {
        super(name, cacheProperties);
        ItemProperties itemProperties = this.getItemProperties();
        if (itemProperties.isEnable()) {
            ExceptionUtil.paramPositive(itemProperties.getTimeout(), "过期时间必须大于0！");
            this.jedis = MiddlewareCreator.createJedis(itemProperties);
        }
        ExceptionUtil.paramNull(serializer, "序列化器不能为空！");
        this.serializer = serializer;
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
    public <T extends Serializable> T get(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        String strObj = jedis.get(key);
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
        return serializer.deserializeFromString(cacheObject.getSerializeStr(), type);
    }

    @Override
    public <T extends Serializable> Map<String, T> getMul(List<String> keys) {
        Map<String, T> result = new HashMap<>(keys.size(), 2);
        if (StringUtils.isEmpty(keys)) {
            return result;
        }
        List<String> strObjList = jedis.mget(keys.toArray(new String[0]));
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
                result.put(keys.get(i), serializer.deserializeFromString(cacheObject.getSerializeStr(), type));
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
                JSON.toJSONString(value),
                System.currentTimeMillis()
        );
        jedis.setex(key, this.getTimeUnitToMillisecond(), JSON.toJSONString(cacheObject));
    }

    @Override
    public <T extends Serializable> void setMul(Map<String, T> map) {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        String[] keyValues = new String[map.size() * 2];
        int i = 0;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            if (StringUtils.isEmpty(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            CacheObject cacheObject = new CacheObject(
                    ((Object) entry.getValue()).getClass().getName(),
                    JSON.toJSONString(entry.getValue()),
                    System.currentTimeMillis()
            );
            keyValues[i] = entry.getKey();
            keyValues[i+1] = JSON.toJSONString(cacheObject);
            i = i+2;
        }
        jedis.mset(keyValues);
        int expireSeconds = this.getTimeUnitToMillisecond();
        map.keySet().forEach(key -> jedis.expire(key, expireSeconds));
        asyncPool.execute(() -> map.keySet().forEach(key -> jedis.expire(key, expireSeconds)));
    }

    @Override
    public void del(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        jedis.del(key);
    }

    @Override
    public void delMul(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        jedis.del(keys.toArray(new String[0]));
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
            throw new InvalidParameterException(this.getName() + "缓存最小支持秒");
        }
        if (result > Integer.MAX_VALUE) {
            throw new InvalidParameterException(this.getName() + "缓存参数超出最大过期时间");
        }
        return (int) result;
    }
}
