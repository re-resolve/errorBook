package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Chapter;
import com.errorbook.errorbookv1.mapper.ChapterMapper;
import com.errorbook.errorbookv1.service.ChapterService;
import org.springframework.stereotype.Service;

@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {
}
