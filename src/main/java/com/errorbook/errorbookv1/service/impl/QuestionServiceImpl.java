package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.entity.Question;
import com.errorbook.errorbookv1.mapper.QuestionMapper;
import com.errorbook.errorbookv1.service.QuestionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
}
