package com.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.User;

// 继承 BaseMapper<User>，MyBatis-Plus 自动帮你实现：
// insert / deleteById / updateById / selectById / selectList / selectPage ...
// 你不需要写任何 SQL 和 XML 文件
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectByIdIncludeDeleted(@Param("id") Long id);

    @Update("UPDATE sys_user SET deleted = 0 WHERE id = #{id}")
    void recoverById(@Param("id") Long id);
}