package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Section;
import com.example.errorBook.mapper.SectionMapper;
import com.example.errorBook.service.SectionService;
import org.springframework.stereotype.Service;

@Service
public class SectionServiceImpl  extends ServiceImpl<SectionMapper, Section> implements SectionService {
}
