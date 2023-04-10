package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Salt;
import com.example.errorBook.mapper.SaltMapper;
import com.example.errorBook.service.SaltService;
import org.springframework.stereotype.Service;

@Service
public class SaltServiceImpl extends ServiceImpl<SaltMapper, Salt> implements SaltService {

}
