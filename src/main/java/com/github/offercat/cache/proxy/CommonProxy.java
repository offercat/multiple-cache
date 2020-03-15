package com.github.offercat.cache.proxy;

import com.github.offercat.cache.config.ItemProperties;
import com.github.offercat.cache.inte.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 公用动态代理
 *
 * @author 徐通 xutong34
 * @since 2019年10月7日09:42:02
 */
@Slf4j
@SuppressWarnings("unchecked")
public class CommonProxy<T> implements MethodInterceptor {

    private AbstractAop aop;

    public T getProxy(Class<T> cls,
                      AbstractAop aop,
                      String cacheName,
                      Serializer serializer,
                      ItemProperties itemProperties) {
        this.aop = aop;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);
        enhancer.setCallback(this);
        return (T) enhancer.create(
                new Class[]{String.class, Serializer.class, ItemProperties.class},
                new Object[]{cacheName, serializer, itemProperties});
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        log.info(method.getName());
        ProxyPoint point = new ProxyPoint(o, method, args);
        try {
            Object result;
            try {
                result = aop.around(point, () -> {
                    aop.before(point);
                    return methodProxy.invoke(o, args);
                });
            } finally {
                aop.after(point);
            }
            aop.afterReturning(point);
            return result;
        } catch (Throwable e) {
            aop.afterThrowing(point, e);
            e.printStackTrace();
        }
        return null;
    }
}
