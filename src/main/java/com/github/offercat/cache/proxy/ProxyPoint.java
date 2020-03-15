package com.github.offercat.cache.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 代理切点包装类
 *
 * @author 徐通 xutong34
 * @since 2019年10月7日10:26:23
 */
@Data
@AllArgsConstructor
public class ProxyPoint {
    private Object proxy;
    private Method method;
    private Object[] args;
}
