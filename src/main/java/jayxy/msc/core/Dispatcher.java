package jayxy.msc.core;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jayxy.msc.annotation.Param;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 请求分发器，处理所有请求
 * 1. 提取请求路由找到对应方法。 -> Router
 * 2. 解析请求参数，调用方法执行
 * 3. 没有对应方法则返回静态资源 -> StaticHandler
 */
@Slf4j
public class Dispatcher {
    private final Router router;
    private final StaticHandler staticHandler;

    public Dispatcher(Router router) {
        this.router = router;
        this.staticHandler = new StaticHandler();
    }

    // 处理所有请求
    public void handle(HttpServletRequest req, HttpServletResponse resp){
        String method = req.getMethod();
        String url = req.getRequestURI().replace(req.getContextPath(), "");
        try {
            // 1. 优先处理API请求
            Router.Handler handler = router.getHandler(method, url);
            if (handler != null) {
                handleApi(req, resp, handler);
                return;
            }
            // 2. 处理静态资源（适配SPA）
            staticHandler.handle(url, resp);
        }catch (Exception e){
            log.error("",e);
            throw new RuntimeException(e);
        }
    }

    // 处理API请求
    private void handleApi(HttpServletRequest req, HttpServletResponse resp, Router.Handler handler) throws IOException {
        try {
            Method method = handler.getMethod();
            // 解析请求参数
            String reqMethod = req.getMethod();
            // 调用参数解析器（支持注入request/response）
            Object[] args = resolveParams(req, resp, method.getParameters());
            // 调用Controller方法
            Object result = method.invoke(handler.getController(), args);
            // 返回JSON响应
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(JSON.toJSONString(result));
        } catch (Exception e) {
            resp.sendError(500, "API处理失败：" + e.getMessage());
        }
    }
    // 通用参数解析方法
    private Object[] resolveParams(HttpServletRequest req, HttpServletResponse resp, Parameter[] parameters){
        return Arrays.stream(parameters)
                .map(param -> {
                    try {
                        return resolveSingleParam(req, resp, param);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray();
    }
    // 解析单个参数
    private Object resolveSingleParam(HttpServletRequest req, HttpServletResponse resp, Parameter param) throws IOException {
        Class<?> paramType = param.getType();

        // 1. 注入HttpServletRequest对象
        if (paramType == HttpServletRequest.class) {
            return req;
        }

        // 2. 注入HttpServletResponse对象
        if (paramType == HttpServletResponse.class) {
            return resp;
        }

        // 3. 注入请求头（通过@Param指定头名称，如@Param("User-Agent")）
        if (param.isAnnotationPresent(Param.class) && paramType == String.class) {
            String paramName = param.getAnnotation(Param.class).value();
            // 先查请求头，再查请求参数（优先头信息）
            String headerValue = req.getHeader(paramName);
            if (headerValue != null) {
                return headerValue;
            }
        }

        // 4. 处理POST请求体（JSON→对象）
        if (req.getMethod().equals("POST") && !paramType.isPrimitive() && !String.class.equals(paramType)) {
            String jsonBody = readJsonBody(req);
            return JSON.parseObject(jsonBody, paramType);
        }

        // 5. 处理GET参数或POST表单参数
        if (param.isAnnotationPresent(Param.class)) {
            String paramName = param.getAnnotation(Param.class).value();
            String value = req.getParameter(paramName);
            // 基本类型转换（简化版）
            if (paramType == Integer.class) {
                return value == null ? null : Integer.parseInt(value);
            }
            return value;
        }

        throw new RuntimeException("不支持的参数类型：" + paramType.getName());
    }
    // 读取POST请求的JSON体
    private String readJsonBody(HttpServletRequest req) throws IOException {
        InputStream is = req.getInputStream();
        byte[] buffer = new byte[req.getContentLength()];
        is.read(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }
}