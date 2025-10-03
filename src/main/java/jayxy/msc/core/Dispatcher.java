package jayxy.msc.core;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jayxy.msc.annotation.Param;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 请求分发器
 */
public class Dispatcher {
    private final Router router;
    private final StaticHandler staticHandler;

    public Dispatcher(Router router) {
        this.router = router;
        this.staticHandler = new StaticHandler();
    }

    // 处理所有请求
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String method = req.getMethod();
        String url = req.getRequestURI().replace(req.getContextPath(), "");

        // 1. 优先处理API请求
        Router.Handler handler = router.getHandler(method, url);
        if (handler != null) {
            handleApi(req, resp, handler);
            return;
        }

        // 2. 处理静态资源（适配SPA）
        staticHandler.handle(url, resp);
    }

    // 处理API请求
    private void handleApi(HttpServletRequest req, HttpServletResponse resp, Router.Handler handler) throws IOException {
        try {
            Method method = handler.getMethod();
            // 解析请求参数
            Object[] args = req.getMethod().equals("POST")
                    ? resolvePostParams(req, method.getParameters())
                    : resolveGetParams(req, method.getParameters());
            // 调用Controller方法
            Object result = method.invoke(handler.getController(), args);
            // 返回JSON响应
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(JSON.toJSONString(result));
        } catch (Exception e) {
            resp.sendError(500, "API处理失败：" + e.getMessage());
        }
    }
    // 解析POST请求的JSON体参数
    private Object[] resolvePostParams(HttpServletRequest req, Parameter[] parameters) throws IOException {
        // 读取请求体
        InputStream is = req.getInputStream();
        byte[] buffer = new byte[req.getContentLength()];
        is.read(buffer);
        String jsonBody = new String(buffer, StandardCharsets.UTF_8);

        // 如果只有一个参数，且是自定义对象，直接解析JSON
        if (parameters.length == 1) {
            Class<?> paramType = parameters[0].getType();
            if (!paramType.isPrimitive() && !String.class.equals(paramType)) {
                return new Object[]{JSON.parseObject(jsonBody, paramType)};
            }
        }

        // 否则视为多参数（从JSON中提取字段）
        return Arrays.stream(parameters)
                .map(param -> {
                    Param paramAnno = param.getAnnotation(Param.class);
                    String paramName = paramAnno != null ? paramAnno.value() : param.getName();
                    // 从JSON中提取参数值（简化实现，实际可使用JSON对象获取）
                    return JSON.parseObject(jsonBody).getObject(paramName, param.getType());
                })
                .toArray();
    }

    // 解析Get请求的@Param参数
    private Object[] resolveGetParams(HttpServletRequest req, Parameter[] parameters) {
        return java.util.Arrays.stream(parameters)
                .map(param -> {
                    Param paramAnno = param.getAnnotation(Param.class);
                    if (paramAnno == null) {
                        throw new RuntimeException("参数" + param.getName() + "需添加@Param注解");
                    }
                    String value = req.getParameter(paramAnno.value());
                    // 简单类型转换
                    if (param.getType() == Integer.class) {
                        return value == null ? null : Integer.parseInt(value);
                    }
                    return value;
                })
                .toArray();
    }
}