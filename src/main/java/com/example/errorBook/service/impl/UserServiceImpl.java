package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.User;
import com.example.errorBook.mapper.UserMapper;
import com.example.errorBook.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
