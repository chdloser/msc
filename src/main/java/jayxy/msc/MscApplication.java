package jayxy.msc;

import jayxy.msc.core.MscLauncher;

/**
 * Web应用启动类
 * 开发者直接调用此类启动应用
 */
public class MscApplication {
    public static void run(Class<?> mainClass, String... args) {
        // 默认扫描mainClass所在包的子包"api"（如mainClass在com.example，则扫描com.example.api）
        String basePackage = mainClass.getPackage().getName() + ".api";
        MscLauncher.start(basePackage);
    }
}