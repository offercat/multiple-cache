package com.github.offercat.cache.broadcast;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Nats 广播消息
 * Nats broadcast message
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月15日 17:49:58
 */
@Data
@AllArgsConstructor
public class BroadcastMessage implements Serializable {

    /**
     * 广播源头，即广播发起的容器标识
     * Broadcast source, i.e. the container ID of broadcast initiation
     */
    private String origin;

    /**
     * 发起广播的缓存名称
     * Cache name of the broadcast
     */
    private String cacheName;

    /**
     * 是否是删除操作
     * Delete operation or not
     */
    private boolean onlyDel;

    /**
     * 广播对象的集合
     * Collection of broadcast objects
     */
    private List<BroadcastObject> broadcastObjects;
}
