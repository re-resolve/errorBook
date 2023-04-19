package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Subject;
import com.errorbook.errorbookv1.mapper.SubjectMapper;
import com.errorbook.errorbookv1.service.SubjectService;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl  extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {
}
