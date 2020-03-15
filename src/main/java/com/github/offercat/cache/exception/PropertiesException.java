package com.github.offercat.cache.exception;

/**
 * 参数配置初始化异常
 * Parameter configuration initialization exception
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019年10月6日21:21:24
 */
public class PropertiesException extends RuntimeException {

    private static final long serialVersionUID = -81937914238214658L;

    public PropertiesException(String message) {
        super(message);
    }
}
