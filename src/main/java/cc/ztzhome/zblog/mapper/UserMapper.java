package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int insertUser(User user);
    User selectAllByEmail(String email);
    //根据邮箱查询用户是否存在
    boolean existsByEmail(@Param("email") String email);
}
