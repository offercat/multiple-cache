package com.github.offercat.cache.inte;

import com.github.offercat.cache.action.BaseAction;
import com.github.offercat.cache.broadcast.Broadcast;
import com.github.offercat.cache.broadcast.WithBroadcast;
import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.extra.CacheEntity;
import com.github.offercat.cache.extra.CacheEntityParser;
import com.github.offercat.cache.extra.ExceptionUtil;
import com.github.offercat.cache.extra.UnSerializeEntityParser;
import com.github.offercat.cache.proxy.UnIntercept;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 抽象缓存，所有缓存都需要集成这个类
 * Abstract cache, all caches need to integrate this class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 15:12:32
 */
@Slf4j
@NoArgsConstructor
public abstract class AbstractCache implements BaseAction, WithBroadcast {

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
     * 缓存实体解析器
     * Cache Entity parser
     */
    private CacheEntityParser cacheEntityParser;

    /**
     * 本缓存的参数配置
     * Parameter configuration of this cache
     */
    private ItemProperties itemProperties;

    public AbstractCache(String name, ItemProperties itemProperties) {
        ExceptionUtil.paramNull(name, "Cache name cannot be null！");
        ExceptionUtil.paramNull(itemProperties, "Cache properties cannot be null！");
        this.order = 1;
        this.name = name;
        this.itemProperties = itemProperties;
        this.cacheEntityParser = new UnSerializeEntityParser();
        if (itemProperties.isEnable()) {
            this.initMiddleware(itemProperties);
        }
    }

    /**
     * 初始化中间件
     * Initialize Middleware
     *
     * @param itemProperties Parameter configuration of this cache
     */
    public abstract void initMiddleware(ItemProperties itemProperties);

    /**
     * 设置并返回下一个缓存节点
     * Set up and return to the next cache node
     *
     * @param next next cache node
     * @return next cache node
     */
    @UnIntercept
    public AbstractCache setNext(AbstractCache next) {
        ExceptionUtil.paramNull(next, "Next node cannot be null！");
        this.next = next;
        next.order = this.order + 1;
        next.prev = this;
        return next;
    }

    @UnIntercept
    public String getName() {
        return name;
    }

    @UnIntercept
    public int getOrder() {
        return order;
    }

    @UnIntercept
    public AbstractCache getNext() {
        return next;
    }

    @UnIntercept
    public AbstractCache getPrev() {
        return prev;
    }

    @UnIntercept
    public ItemProperties getItemProperties() {
        return itemProperties;
    }

    @UnIntercept
    public CacheEntityParser getCacheEntityParser() {
        return cacheEntityParser;
    }

    @UnIntercept
    public void setCacheEntityParser(CacheEntityParser cacheEntityParser) {
        this.cacheEntityParser = cacheEntityParser;
    }

    @Override
    @Broadcast(type = Broadcast.OperationType.SET_ONE)
    public void setWithBroadcast(String key, CacheEntity cacheEntity) {
        this.setCacheEntity(key, cacheEntity);
    }

    @Override
    @Broadcast(type = Broadcast.OperationType.SET_MUL)
    public void setMulWithBroadcast(Map<String, CacheEntity> keyObjects) {
        this.setMulCacheEntity(keyObjects);
    }

    @Override
    @Broadcast(type = Broadcast.OperationType.DEL_ONE)
    public void delWithBroadcast(String key) {
        this.del(key);
    }

    @Override
    @Broadcast(type = Broadcast.OperationType.DEL_MUL)
    public void delMulWithBroadcast(List<String> keys) {
        this.delMul(keys);
    }
}
