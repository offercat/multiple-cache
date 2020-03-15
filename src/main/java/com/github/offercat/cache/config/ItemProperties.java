package com.github.offercat.cache.config;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 单个缓存的配置
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 15:23:42
 */
@Data
public class ItemProperties {

    /** 缓存地址 */
    private String address;

    /** 端口 */
    private int port;

    /** 缓存密码 */
    private String password;

    /** 超时时间 */
    private long timeout;

    /** 超时单位 */
    private TimeUnit timeunit;

    /** 本地缓存的最大数量 */
    private int maxSize = 2000;

    /** 是否开启 */
    private boolean enable = false;

    /** 是否开启广播 */
    private boolean broadcastEnable = false;

    private Map<String, String> expand;

    /** 本地缓存 key 过期模式，默认访问不刷新过期时间 */
    private ExpireMode expireMode = ExpireMode.TTL;

    /**
     * 本地缓存过期模式
     */
    public enum ExpireMode {

        /** 访问不刷新过期时间 */
        TTL,

        /** 访问刷新过期时间 */
        TTI
    }
}
