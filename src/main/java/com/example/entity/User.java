package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.baomidou.mybatisplus.annotation.TableLogic;

// @TableName 告诉 MyBatis-Plus 这个类对应数据库里的哪张表
@TableName("sys_user")
public class User {

    // @TableId 标记主键，IdType.AUTO 表示主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    @TableLogic
    @JsonIgnore
    private Integer deleted;

    private String email;

    // Getter / Setter（MyBatis-Plus 底层通过 getter/setter 读写字段）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}