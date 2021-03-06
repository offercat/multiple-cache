package com.github.offercat.cache.inte;

import com.github.offercat.cache.action.MapAction;
import com.github.offercat.cache.action.ListAction;
import com.github.offercat.cache.action.SetAction;
import com.github.offercat.cache.action.ZsetAction;
import com.github.offercat.cache.config.ItemProperties;
import lombok.NoArgsConstructor;

/**
 * 抽象集群缓存，所有集群缓存继承这个抽象类
 * Abstract cluster cache. All cluster caches inherit this abstract class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 16:34:45
 */
@NoArgsConstructor
public abstract class ClusterCache extends AbstractCache implements MapAction, ListAction, SetAction, ZsetAction {

    public ClusterCache(String name, ItemProperties itemProperties){
        super(name, itemProperties);
    }
}
