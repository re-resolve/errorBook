package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Chapter;
import com.example.errorBook.mapper.ChapterMapper;
import com.example.errorBook.service.ChapterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {
    @Override
    public List<Chapter> selectChapterByConditons(Chapter chapter, Long subjectId) {

        return null;
    }
}
