package jayxy.msc.core;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import java.io.IOException;

/**
 * 框架启动器
 */
public class MscLauncher {
    private static final int DEFAULT_PORT = 8080;
    private static String scanPackage; // 要扫描的Controller包

    // 启动框架
    public static void start(int port, String packageToScan) {
        scanPackage = packageToScan;
        try {
            Server server = new Server(port);
            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            context.addServlet(new ServletHolder(new MscServlet()), "/*"); // 核心Servlet
            server.setHandler(context);
            server.start();
            System.out.println("MSC框架启动成功！端口：" + port + "，扫描包：" + scanPackage);
            server.join();
        } catch (Exception e) {
            throw new RuntimeException("MSC框架启动失败", e);
        }
    }

    /**
     * 简化启动（默认端口8080）
     * @param packageToScan 扫包路径
     */

    public static void start(String packageToScan) {
        start(DEFAULT_PORT, packageToScan);
    }

    // 框架核心Servlet
    private static class MscServlet extends HttpServlet {
        private final Router router = new Router();
        private final Dispatcher dispatcher;

        public MscServlet() {
            router.scan(scanPackage); // 初始化路由
            dispatcher = new Dispatcher(router);
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            dispatcher.handle(req, resp); // 分发请求
        }
    }
}