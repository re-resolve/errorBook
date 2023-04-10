package com.example.errorBook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.errorBook.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
