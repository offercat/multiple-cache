package com.github.offercat.cache.extra;

/**
 * 带有抛出异常的事件
 *
 * @author 徐通 xutong34
 * @since 2019/10/8 10:14
 */
@FunctionalInterface
public interface ThrowFunction<T> {

    /**
     * 获取执行结果
     *
     * @return 执行结果
     * @throws Throwable 异常
     */
    T get() throws Throwable;
}
