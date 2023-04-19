package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Salt;
import com.errorbook.errorbookv1.mapper.SaltMapper;
import com.errorbook.errorbookv1.service.SaltService;
import org.springframework.stereotype.Service;

@Service
public class SaltServiceImpl extends ServiceImpl<SaltMapper, Salt> implements SaltService {

}
