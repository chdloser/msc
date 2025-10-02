package jayxy.example.api;

import jayxy.msc.annotation.Api;
import jayxy.msc.annotation.Get;
import jayxy.msc.annotation.Param;
import jayxy.example.model.User;
import java.util.Arrays;
import java.util.List;

// Controller层：API接口
@Api("/user") // 路径前缀：/user
public class UserController {
    // API接口：/user/list
    @Get("/list")
    public List<User> getUserList() {
        return Arrays.asList(
                new User(1, "张三"),
                new User(2, "李四")
        );
    }

    // API接口：/user/info?id=xxx
    @Get("/info")
    public User getUserInfo(@Param("id") Integer id) {
        return new User(id, "用户" + id);
    }
}