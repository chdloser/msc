package jayxy.msc.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 类扫描器
 */
public class ClassScanner {
    // 扫描指定包下的所有类
    public static List<Class<?>> scan(String basePackage) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = basePackage.replace(".", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(packagePath);
        if (url == null) return classes;

        File dir = new File(url.toURI());
        if (dir.isDirectory()) {
            scanDir(dir, basePackage, classes);
        }
        return classes;
    }

    // 递归扫描目录
    private static void scanDir(File dir, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDir(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }
}