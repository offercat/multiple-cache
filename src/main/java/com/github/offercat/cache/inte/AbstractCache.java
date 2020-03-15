package com.github.offercat.cache.inte;

import com.github.offercat.cache.action.BaseAction;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.exception.ExceptionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象缓存，所有缓存都需要集成这个类
 * Abstract cache, all caches need to integrate this class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 15:12:32
 */
@Data
@Slf4j
public abstract class AbstractCache implements BaseAction {

    /**
     * 缓存名称
     * cache name
     */
    private String name;

    /**
     * 缓存编码，记录缓存链的顺序
     * Cache order encoding, recording the order of the cache chain
     */
    private int order;

    /**
     * 下一个缓存节点
     * Next cache node
     */
    private AbstractCache next;

    /**
     * 上一个节点
     * Previous cache node
     */
    private AbstractCache prev;

    /**
     * 本缓存的参数配置
     * Parameter configuration of this cache
     */
    private ItemProperties itemProperties;

    public AbstractCache(String name, CacheProperties cacheProperties) {
        ExceptionUtil.paramNull(name, "缓存名称不能为空！");
        ExceptionUtil.paramNull(cacheProperties, "缓存配置不能为空！");
        ExceptionUtil.paramNull(cacheProperties.getConfig(), "缓存配置不能为空！");
        this.order = 1;
        this.name = name;
        cacheProperties.getConfig().forEach((key, itemProperties) -> {
            if (this.name.equals(key)) {
                this.itemProperties = itemProperties;
            }
        });
        ExceptionUtil.paramNull(itemProperties, "找不到指定名称的缓存配置！");
    }

    /**
     * 设置并返回下一个缓存节点
     * Set up and return to the next cache node
     *
     * @param next next cache node
     * @return next cache node
     */
    public AbstractCache setNext(AbstractCache next) {
        ExceptionUtil.paramNull(next, "节点不能为空！");
        this.next = next;
        this.next.setOrder(this.order + 1);
        this.next.prev = this;
        return next;
    }
}
