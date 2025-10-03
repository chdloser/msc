package jayxy.msc;

import jayxy.msc.core.MscLauncher;

/**
 * Web应用启动类
 * 开发者直接调用此类启动应用
 */
public class MscApplication {
    public static void run(Class<?> mainClass, String... args) {
        // 默认扫描mainClass所在包及子包
        if(args.length == 0) {
            String basePackage = mainClass.getPackage().getName();
            MscLauncher.start(basePackage);
        }else {
            for(String arg : args) {
                MscLauncher.start(arg);
            }
        }

    }
}