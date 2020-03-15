package com.github.offercat.cache.extra;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Map;

/**
 * 异常处理工具类
 * Exception handling tool class
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2020年03月14日 15:29:23
 */
public class ExceptionUtil {

    /**
     * 判断对象是否为 null，如果是 null，抛出非法参数异常
     * Judge whether the object is null. If it is null, throw InvalidParameterException
     *
     * @param obj     object
     * @param message Custom exception message
     */
    public static void paramNull(Object obj, String message) {
        if (isNull(obj)) {
            throw new InvalidParameterException(message);
        }
    }

    /**
     * 判断数字是否为正数，如果是负数，抛出非法参数异常
     * Judge whether the number is positive. If it is negative, throw InvalidParameterException
     *
     * @param param   number
     * @param message Custom exception message
     */
    public static void paramPositive(long param, String message) {
        if (param <= 0) {
            throw new InvalidParameterException(message);
        }
    }

    /**
     * Judge null
     *
     * @param obj obj
     * @return true or false
     */
    public static Boolean isNull(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            Object[] objects = (Object[]) obj;
            if (objects.length == 0) {
                return true;
            }
            boolean empty = true;
            for (Object object : objects) {
                if (!isNull(object)) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }
        return false;
    }
}
