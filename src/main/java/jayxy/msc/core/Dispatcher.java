package jayxy.msc.core;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jayxy.msc.annotation.Param;
import java.io.IOException;
import java.lang.reflect.Parameter;

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
            // 解析请求参数
            Object[] args = resolveParams(req, handler.getMethod().getParameters());
            // 调用Controller方法
            Object result = handler.getMethod().invoke(handler.getController(), args);
            // 返回JSON响应
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(JSON.toJSONString(result));
        } catch (Exception e) {
            resp.sendError(500, "API处理失败：" + e.getMessage());
        }
    }

    // 解析@Param参数
    private Object[] resolveParams(HttpServletRequest req, Parameter[] parameters) {
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