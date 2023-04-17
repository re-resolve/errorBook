package com.example.errorBook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.errorBook.entity.Section;

import java.util.List;

public interface SectionService extends IService<Section> {
    List<Section> selectSectionByConditions(Section section, Long chapterId);
}
