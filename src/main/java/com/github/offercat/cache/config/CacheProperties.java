package com.github.offercat.cache.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 缓存配置引入
 * Cache configuration parameters integration
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 01:16:41
 */
@Data
@Slf4j
@ConfigurationProperties("multiple.cache")
public class CacheProperties {

    /**
     * 是否开启缓存日志
     * Open cache log or not
     */
    private boolean logEnable = false;

    /**
     * 广播nats地址
     * Nats broadcast address
     */
    private String natsUri;

    /**
     * 广播主题
     * Broadcast topic
     */
    private String broadcastTopic = "";

    /**
     * 各级缓存的配置映射
     * Configuration parameters mapping of all levels of cache
     */
    private Map<String, ItemProperties> config;
}
