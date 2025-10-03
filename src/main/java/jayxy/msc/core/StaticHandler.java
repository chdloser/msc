package jayxy.msc.core;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 静态资源处理器
 */
public class StaticHandler {
    private static final String STATIC_DIR = "static"; // 静态资源根目录

    public void handle(String url, HttpServletResponse resp) throws IOException {
        // 适配SPA history模式：非文件路径返回index.html
        if (isSpaRoute(url)) {
            url = "/index.html";
        }

        // 读取资源文件
        String resourcePath = STATIC_DIR + (url.startsWith("/") ? url : "/" + url);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                resp.sendError(404, "静态资源不存在：" + url);
                return;
            }

            // 设置响应类型
            setContentType(url, resp);

            // 写入响应
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                resp.getOutputStream().write(buffer, 0, len);
            }
        }
    }

    // 判断是否为SPA路由（非资源文件）
    private boolean isSpaRoute(String url) {
        return !url.contains(".") && !url.equals("/");
    }

    // 设置Content-Type
    private void setContentType(String url, HttpServletResponse resp) {
        if (url.endsWith(".html")) resp.setContentType("text/html;charset=UTF-8");
        else if (url.endsWith(".css")) resp.setContentType("text/css;charset=UTF-8");
        else if (url.endsWith(".js")) resp.setContentType("application/javascript;charset=UTF-8");
        else if (url.endsWith(".json")) resp.setContentType("application/json;charset=UTF-8");
        else if (url.endsWith(".png")) resp.setContentType("image/png");
    }
}