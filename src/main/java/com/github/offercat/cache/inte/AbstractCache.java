package com.github.offercat.cache.inte;

import com.github.offercat.cache.action.BaseAction;
import com.github.offercat.cache.config.CacheProperties;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.exception.ExceptionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象缓存
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 15:12:32
 */
@Data
@Slf4j
public abstract class AbstractCache implements BaseAction {

    /** 节点名字 */
    private String name;

    /** 节点编码 */
    private int code;

    /** 下一个节点 */
    private AbstractCache next;

    /** 上一个节点 */
    private AbstractCache prev;

    /** 缓存配置 */
    private ItemProperties itemProperties;

    public AbstractCache(String name, CacheProperties cacheProperties) {
        ExceptionUtil.paramNull(name, "缓存名称不能为空！");
        ExceptionUtil.paramNull(cacheProperties, "缓存配置不能为空！");
        ExceptionUtil.paramNull(cacheProperties.getConfig(), "缓存配置不能为空！");
        this.code = 1;
        this.name = name;
        cacheProperties.getConfig().forEach((key, itemProperties) -> {
            if (this.name.equals(key)) {
                this.itemProperties = itemProperties;
            }
        });
        ExceptionUtil.paramNull(itemProperties, "找不到指定名称的缓存配置！");
    }

    /**
     * 设置下一个节点并返回
     *
     * @param next 下一个节点
     * @return 下一个节点
     */
    public AbstractCache setNext(AbstractCache next) {
        ExceptionUtil.paramNull(next, "节点不能为空！");
        this.next = next;
        this.next.setCode(this.code + 1);
        this.next.prev = this;
        return next;
    }
}
