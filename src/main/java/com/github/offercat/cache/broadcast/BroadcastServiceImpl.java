package com.github.offercat.cache.broadcast;

import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.MiddlewareCreator;
import com.github.offercat.cache.extra.CacheObject;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.inte.AbstractCache;
import com.github.offercat.cache.inte.Serializer;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 广播服务实现
 * Broadcast service implementation
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月15日 18:28:17
 */
@Slf4j
public class BroadcastServiceImpl implements BroadcastService {

    private final String topic;
    private final Connection connection;
    private final Serializer serializer;
    private final List<AbstractCache> cacheList;

    public BroadcastServiceImpl(CacheProperties properties, Serializer serializer, List<AbstractCache> cacheList) {
        ExceptionUtil.paramNull(properties, "cacheProperties can not be null!");
        ExceptionUtil.paramNull(serializer, "serializer can not be null!");
        ExceptionUtil.paramNull(cacheList, "cache list can not be null!");
        ExceptionUtil.paramNull(properties.getNatsUri(), "nats uri can not be null!");
        this.topic = TOPIC + properties.getBroadcastTopic().trim();
        this.connection = MiddlewareCreator.getConnection(properties);
        this.serializer = serializer;
        this.cacheList = cacheList;
        Dispatcher dispatcher = connection.createDispatcher(
                msg -> this.dequeue(serializer.deserializeFromBytes(msg.getData()))
        );
        log.info("Start listening to cache broadcast");
        dispatcher.subscribe(topic);
    }

    @Override
    public void enqueue(BroadcastMessage message) {
        connection.publish(topic, serializer.serializeToBytes(message));
    }

    @Override
    public void dequeue(BroadcastMessage message) {
        if (Objects.equals(message.getOrigin(), THIS_SERVER_FLAG)) {
            log.debug("receive native message keys.size = {}", message.getBroadcastObjects().size());
            return;
        }
        AbstractCache cache = this.getCache(message.getCacheName());
        if (cache == null) {
            log.info("dequeue | A broadcast message was received, but the specified cache could not be found " +
                    "message = {} ", message);
            return;
        }
        List<BroadcastObject> broadcastObjects = message.getBroadcastObjects();
        List<String> keys = broadcastObjects.stream().map(BroadcastObject::getKey).collect(Collectors.toList());
        if (message.isOnlyDel()) {
            log.info("--- cache sync ---  收到 nats 广播消息，清除本地缓存 {} keys = {} ", keys.size(), keys);
            cache.delMul(keys);
            return;
        }

        Map<String, CacheObject> cacheMap = cache.getMulCacheObject(keys);
        broadcastObjects.forEach(broadcastObject -> {
            if (broadcastObject.getCacheObject() == null) {
                return;
            }
            CacheObject cacheObject = cacheMap.get(broadcastObject.getKey());
            if (cacheObject != null && Objects.equals(cacheObject.getObject(), broadcastObject.getCacheObject())) {
                log.info("--- cache sync ---  缓存内容一致，不需要替换 key = {}", broadcastObject.getKey());
                return;
            }
            if (cacheObject == null || cacheObject.getSetTime() < broadcastObject.getCacheObject().getSetTime()) {
                log.info("--- cache sync ---  更新的缓存对象  key = {}", broadcastObject.getKey());
                cache.setCacheObject(broadcastObject.getKey(), broadcastObject.getCacheObject());
            }
        });
    }

    private AbstractCache getCache(String cacheName) {
        if (cacheName == null) {
            log.info("getCache | cache name is null");
            return null;
        }
        for (AbstractCache cache: cacheList) {
            if (cacheName.equals(cache.getName())) {
                return cache;
            }
        }
        return null;
    }
}
