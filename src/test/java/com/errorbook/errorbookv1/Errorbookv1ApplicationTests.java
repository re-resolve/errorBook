package com.errorbook.errorbookv1;

import com.errorbook.errorbookv1.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Errorbookv1ApplicationTests {
	@Autowired
	private QuestionService questionService;
	@Test
	void contextLoads() {
		System.out.println(questionService.listQuestionTitle(0L, 0L, 5L));
	}

}
