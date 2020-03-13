package com.github.offercat.cache.config;

import com.github.offercat.cache.MultipleCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * spring boot 自动配置
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 01:09:34
 */
@Configuration
@ConditionalOnClass({MultipleCache.class})
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfig {

    @Resource
    private CacheProperties properties;
}
