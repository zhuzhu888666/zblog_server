package cc.ztzhome.zblog.mapper;

import cc.ztzhome.zblog.bean.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserMapper {
    int insertUser(User user);
    //根据邮箱查询用户
    User selectAllByEmail(String email);
    //根据ID查询用户
    User selectById(Long userId);
    //根据邮箱查询用户是否存在
    boolean existsByEmail(@Param("email") String email);
    //更新用户密码
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    //更新用户资料
    int updateProfile(@Param("userId") Long userId,
                      @Param("nickname") String nickname,
                      @Param("gender") Integer gender,
                      @Param("introduction") String introduction,
                      @Param("birthday") LocalDate birthday);

    //更新用户头像
    int updateAvatar(@Param("userId") Long userId, @Param("userAvatar") String userAvatar);

    // ==================== 管理员功能 ====================

    List<User> selectAll();

    List<User> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    long countAll();

    int updateUser(User user);

    int deleteById(@Param("userId") Long userId);

    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);
}
