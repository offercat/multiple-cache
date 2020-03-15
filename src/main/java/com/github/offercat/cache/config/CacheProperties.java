package com.github.offercat.cache.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 缓存配置引入
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 01:16:41
 */
@Data
@Slf4j
@ConfigurationProperties("multiple.cache")
public class CacheProperties {

    /** 是否开启缓存日志 */
    private boolean logEnable = false;

    /** 广播nats地址 */
    private String natsUri;

    /** 广播主题 */
    private String broadcastTopic = "";

    /** 缓存名称和单个缓存配置的映射 */
    private Map<String, ItemProperties> config;
}
