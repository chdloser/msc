package jayxy.msc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Api {
    String value() default ""; // 例如：@Api("/user") → 所有方法路径前缀为/user
}