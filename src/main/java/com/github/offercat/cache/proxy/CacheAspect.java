package com.github.offercat.cache.proxy;

import com.github.offercat.cache.broadcast.Broadcast;
import com.github.offercat.cache.broadcast.BroadcastMessage;
import com.github.offercat.cache.broadcast.BroadcastObject;
import com.github.offercat.cache.broadcast.BroadcastService;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.extra.CacheObject;
import com.github.offercat.cache.extra.ThrowFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.github.offercat.cache.broadcast.BroadcastService.THIS_SERVER_FLAG;

/**
 * Description
 *
 * @author 徐通 xutong34
 * @since 2020年03月15日 20:29:12
 */
@Slf4j
@SuppressWarnings("unchecked")
public class CacheAspect extends AbstractAop {

    private String cacheName;
    private CacheProperties properties;
    private ItemProperties itemProperties;
    private BroadcastService broadcastService;

    public CacheAspect(String cacheName, CacheProperties properties, BroadcastService broadcastService) {
        this.cacheName = cacheName;
        this.properties = properties;
        this.itemProperties = properties.getConfig().get(cacheName);
        if (properties.isBroadcastEnable()) {
            this.broadcastService = broadcastService;
        }
    }

    @Override
    public Object around(ProxyPoint point, ThrowFunction<Object> function) throws Throwable {
        if (itemProperties.isEnable()) {
            return function.get();
        }
        return null;
    }

    @Override
    public void afterReturning(ProxyPoint point) {
        if (properties.isBroadcastEnable() && itemProperties.isBroadcastEnable() && broadcastService != null) {
            Broadcast broadcast = point.getMethod().getAnnotation(Broadcast.class);
            if (broadcast == null) {
                return;
            }
            if (Broadcast.OperationType.SET_ONE.equals(broadcast.type())) {
                this.setOne(point);
            } else if (Broadcast.OperationType.SET_MUL.equals(broadcast.type())) {
                this.setMul(point);
            } else if (Broadcast.OperationType.DEL_ONE.equals(broadcast.type())) {
                this.delOne(point);
            } else if (Broadcast.OperationType.DEL_MUL.equals(broadcast.type())) {
                this.delMul(point);
            } else {
                log.warn("doAfterLocalCacheChange | unknown broadcast type");
            }
        }
    }

    private void setOne(ProxyPoint point) {
        CacheObject cacheObject = (CacheObject) point.getArgs()[1];
        BroadcastObject broadcastObject = new BroadcastObject((String) point.getArgs()[0], cacheObject);
        broadcastService.enqueue(new BroadcastMessage(
                THIS_SERVER_FLAG,
                cacheName,
                false,
                Collections.singletonList(broadcastObject)
        ));
    }

    private void setMul(ProxyPoint point) {
        Map<String, CacheObject> cacheObjectMap = (Map<String, CacheObject>) point.getArgs()[0];
        List<BroadcastObject> broadcastObjectList = new ArrayList<>(cacheObjectMap.size());
        cacheObjectMap.forEach((key, cacheObject) ->
                broadcastObjectList.add(new BroadcastObject(key, cacheObject))
        );
        broadcastService.enqueue(new BroadcastMessage(THIS_SERVER_FLAG, cacheName, false, broadcastObjectList));
    }

    private void delOne(ProxyPoint point) {
        BroadcastObject broadcastObject = new BroadcastObject( (String) point.getArgs()[0], null);
        broadcastService.enqueue(new BroadcastMessage(
                THIS_SERVER_FLAG,
                cacheName,
                true,
                Collections.singletonList(broadcastObject))
        );
    }

    private void delMul(ProxyPoint point) {
        Collection<String> keys = (Collection<String>) point.getArgs()[0];
        List<BroadcastObject> broadcastObjectList = new ArrayList<>(keys.size());
        keys.forEach(key ->
                broadcastObjectList.add(new BroadcastObject((String) key, null))
        );
        broadcastService.enqueue(new BroadcastMessage(
                THIS_SERVER_FLAG,
                cacheName,
                true,
                broadcastObjectList
        ));
    }
}
