package com.github.offercat.cache.broadcast;

import java.util.UUID;

/**
 * 广播服务接口
 * Broadcast service interface
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019年10月6日21:21:24
 */
public interface BroadcastService {

    /**
     * 本服务标识
     * This server container ID flag
     */
    String THIS_SERVER_FLAG = UUID.randomUUID().toString();

    /**
     * 广播主题版本号，如果新版本升级导致广播的消息无法兼容，则通过版本号升级来防止消息冲突
     * Broadcast topic version. If the new version upgrade results in incompatible broadcast messages,
     * the version number upgrade is used to prevent message conflicts
     */
    String TOPIC_VERSION = "1.0";

    /**
     * 订阅主题，对用户指定的 topic 进行一些封装，防止 topic 过于简单或者用户不设置 topic
     * Subscribe to the topic, encapsulate the topic specified by the user to prevent the topic from being too simple or empty
     */
    String TOPIC = "multiple.cache.sync:" + TOPIC_VERSION + ":";

    /**
     * 消息入队
     * Message enqueue
     *
     * @param message BroadcastMessage
     */
    void enqueue(BroadcastMessage message);


    /**
     * 消息出队
     * Message dequeue
     *
     * @param message BroadcastMessage
     */
    void dequeue(BroadcastMessage message);
}
