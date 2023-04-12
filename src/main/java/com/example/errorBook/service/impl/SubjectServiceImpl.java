package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Subject;
import com.example.errorBook.mapper.SubjectMapper;
import com.example.errorBook.service.SubjectService;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl  extends ServiceImpl<SubjectMapper, Subject> implements SubjectService  {
}
