package com.github.offercat.cache.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 公用动态代理
 *
 * @author 徐通 xutong34
 * @since 2019年10月7日09:42:02
 */
@Slf4j
@SuppressWarnings("unchecked")
public class CommonProxy<T> implements MethodInterceptor {

    private T target;
    private AbstractAop aop;
    private static final List<String> INTERCEPT_WHITE_LIST = Arrays.asList(
            "toString",
            "supportBroadcast",
            "initMiddleware"
    );

    public T getProxy(T target, AbstractAop aop) {
        this.target = target;
        this.aop = aop;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        UnIntercept unIntercept = method.getAnnotation(UnIntercept.class);
        if (unIntercept != null || INTERCEPT_WHITE_LIST.contains(method.getName())) {
            return methodProxy.invoke(target, args);
        }
        ProxyPoint point = new ProxyPoint(o, method, args);
        try {
            Object result;
            try {
                result = aop.around(point, () -> {
                    aop.before(point);
                    return methodProxy.invoke(target, args);
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
