package com.github.offercat.cache.proxy;

import com.github.offercat.cache.extra.ThrowFunction;

/**
 * 统一aop接口
 *
 * @author 徐通 xutong34
 * @since 2019年10月7日09:54:47
 */
public abstract class AbstractAop {

    public void before(ProxyPoint point) {}

    public Object around(ProxyPoint point, ThrowFunction<Object> function) throws Throwable {
        return function.get();
    }

    public void after(ProxyPoint point) {}

    public void afterReturning(ProxyPoint point) {}

    public void afterThrowing(ProxyPoint point, Throwable e) {}
}
