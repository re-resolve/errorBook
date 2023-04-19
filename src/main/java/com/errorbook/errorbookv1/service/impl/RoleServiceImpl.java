package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Role;
import com.errorbook.errorbookv1.mapper.RoleMapper;
import com.errorbook.errorbookv1.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
