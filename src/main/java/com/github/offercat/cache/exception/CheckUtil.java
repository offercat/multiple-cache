package com.github.offercat.cache.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/**
 * 判空工具类
 *
 * @author sudianyuan/xutong34
 * @since 2019/9/12 16:59
 */
@Slf4j
public class CheckUtil {

    /**
     * 判空
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
