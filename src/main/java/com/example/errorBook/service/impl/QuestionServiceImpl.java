package com.example.errorBook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.errorBook.entity.Question;
import com.example.errorBook.mapper.QuestionMapper;
import com.example.errorBook.service.QuestionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

}
