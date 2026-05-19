package cc.ztzhome.zblog;

import cc.ztzhome.zblog.bean.entity.User;
import cc.ztzhome.zblog.mapper.UserMapper;
import cc.ztzhome.zblog.utils.BCryptUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class ZblogApplicationTests {
    @Autowired
    UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println("你干嘛，哎哟");
    }

    @Test
    void rg(){
        User user = new User();
        user.setEmail("123456@qq.com");
        user.setRole(2);
        user.setPassword(BCryptUtil.encrypt("123456"));
        user.setNickname("admin");
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insertUser(user);
    }

}
