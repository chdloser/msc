package jayxy.msc.annotation;

import java.lang.annotation.*;

/**
 * 请求参数，支持
 * 请求头参数
 * Get请求参数
 * Post表单参数
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value(); // 例如：@Param("id") → 对应URL中的?id=123
}