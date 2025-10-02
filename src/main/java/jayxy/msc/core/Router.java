package jayxy.msc.core;

import jayxy.msc.annotation.Api;
import jayxy.msc.annotation.Get;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {
    // 路由映射：键=HTTP方法+URL（如"GET_/user/list"），值=处理方法信息
    private final Map<String, Handler> routeMap = new HashMap<>();

    // 扫描指定包下的@Api类，初始化路由
    public void scan(String basePackage) {
        try {
            // 扫描包下所有类
            for (Class<?> clazz : ClassScanner.scan(basePackage)) {
                if (clazz.isAnnotationPresent(Api.class)) {
                    Api api = clazz.getAnnotation(Api.class);
                    String basePath = normalizePath(api.value()); // 处理前缀路径
                    Object controller = clazz.getDeclaredConstructor().newInstance(); // 实例化Controller

                    // 遍历类中所有@Get方法
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Get.class)) {
                            Get get = method.getAnnotation(Get.class);
                            String path = normalizePath(get.value()); // 处理方法路径
                            String fullPath = basePath + path; // 完整URL路径
                            String key = "GET_" + fullPath; // 路由键

                            routeMap.put(key, new Handler(controller, method));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("路由初始化失败", e);
        }
    }

    // 获取路由处理器
    public Handler getHandler(String httpMethod, String url) {
        return routeMap.get(httpMethod + "_" + normalizePath(url));
    }

    // 标准化路径（确保以/开头，去掉重复/）
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "";
        if (!path.startsWith("/")) path = "/" + path;
        return path.replaceAll("//+", "/");
    }

    // 路由处理器：存储Controller实例和方法
    public static class Handler {
        private final Object controller;
        private final Method method;

        public Handler(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }

        public Object getController() { return controller; }
        public Method getMethod() { return method; }
    }
}