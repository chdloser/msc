package jayxy.msc.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    String value(); // 例如：@Get("/list") → 完整路径为/api前缀+/list
}