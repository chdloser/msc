package jayxy.msc.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value(); // 例如：@Param("id") → 对应URL中的?id=123
}