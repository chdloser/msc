package jayxy.example;

import jayxy.msc.MscApplication;

// 应用启动入口
public class Application {
    public static void main(String[] args) {
        // 启动MSC框架，自动扫描当前类所在包下的"api"子包（即jayxy.example.api）
        MscApplication.run(Application.class);
    }
}