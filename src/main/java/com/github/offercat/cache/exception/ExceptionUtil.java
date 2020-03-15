package com.github.offercat.cache.exception;

import java.security.InvalidParameterException;

/**
 * 异常工具类
 *
 * @author 徐通 xutong34
 * @since 2020年03月14日 15:29:23
 */
public class ExceptionUtil {

    public static void paramNull(Object obj, String message) {
        if (CheckUtil.isNull(obj)) {
            throw new InvalidParameterException(message);
        }
    }

    public static void paramPositive(long param, String message) {
        if (param <= 0) {
            throw new InvalidParameterException(message);
        }
    }
}
