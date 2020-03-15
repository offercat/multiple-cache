package com.github.offercat.cache.config;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 单级缓存的配置参数，列举了一些常用的参数
 * Configuration parameters of single level cache，some commonly used parameters are listed
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 15:23:42
 */
@Data
public class ItemProperties {

    /**
     * 缓存地址，提供给有长连接的缓存使用，例如 Redis
     * Cache connection address, which is provided for the cache with Keep-Alive, such as redis
     */
    private String address;

    /**
     * 端口，提供给有长连接的缓存使用，例如 Redis
     * Cache connection port, which is provided for the cache with Keep-Alive, such as redis
     */
    private int port;

    /**
     * 缓存密码，提供给需要鉴权的缓存使用
     * Cache password is provided for the cache requiring authentication
     */
    private String password;

    /**
     * 本缓存统一过期时间
     * Uniform expiration time of this cache
     */
    private long timeout;

    /**
     * 统一过期时间的时间单位
     * Time unit of uniform expiration time
     */
    private TimeUnit timeunit;

    /**
     * 缓存最大容量，在一些有对象容量限制的缓存会用到，例如 Caffeine
     * The maximum cache capacity of the time unit of the unified expiration time is used in some caches with object capacity restrictions, such as caffeine
     */
    private int maxSize = 2000;

    /**
     * 缓存开关，这个参数是动态的，可以在运行时生效
     * Cache switch. This parameter is dynamic and can take effect at runtime
     */
    private boolean enable = false;

    /**
     * 是否针对本缓存开启同步广播
     * Enable synchronous broadcast for this cache
     */
    private boolean broadcastEnable = false;

    /**
     * 缓存过期模式，默认访问不刷新过期时间
     * Cache expiration mode, default access does not refresh expiration time
     */
    private ExpireMode expireMode = ExpireMode.TTL;

    /**
     * 自定义拓展参数，在自定义缓存时，如果以上参数不够用，可以通过这个Map拓展
     * When customizing the cache, if the above parameters are not enough, you can expand through this map
     */
    private Map<String, String> expand;

    /**
     * 缓存过期模式
     * Cache expiration mode
     */
    public enum ExpireMode {

        /**
         * 在访问时不刷新过期时间
         * Do not refresh expiration time on access
         */
        TTL,

        /**
         * 在访问时刷新过期时间
         * Refresh expiration time on access
         */
        TTI
    }
}
