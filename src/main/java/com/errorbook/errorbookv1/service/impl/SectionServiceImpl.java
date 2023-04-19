package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Section;
import com.errorbook.errorbookv1.mapper.SectionMapper;
import com.errorbook.errorbookv1.service.SectionService;
import org.springframework.stereotype.Service;

@Service
public class SectionServiceImpl  extends ServiceImpl<SectionMapper, Section> implements SectionService {
}
