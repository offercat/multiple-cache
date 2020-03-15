package com.github.offercat.cache.broadcast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 广播标识
 * Broadcast identification
 *
 * @author 徐通 Tony Xu myimpte@163.com
 * @since 2019年9月22日02:23:22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Broadcast {

  /**
   * 操作类型，默认为设置单个对象
   * Operation type, default value is SET_ONE
   */
  OperationType type() default OperationType.SET_ONE;

  /**
   * 广播支持的操作类型枚举
   * Enumeration of operation types supported by broadcast
   */
  enum OperationType {

    /**
     * 设置单个
     * Set a single
     */
    SET_ONE,

    /**
     * 设置多个
     * Set multiple
     */
    SET_MUL,

    /**
     * 删除单个
     * Delete a single
     */
    DEL_ONE,

    /**
     * 删除多个
     * Delete multiple
     */
    DEL_MUL
  }
}
