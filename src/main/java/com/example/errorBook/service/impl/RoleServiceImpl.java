package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Role;
import com.example.errorBook.mapper.RoleMapper;
import com.example.errorBook.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
