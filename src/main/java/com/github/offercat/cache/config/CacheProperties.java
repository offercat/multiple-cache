package com.github.offercat.cache.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
