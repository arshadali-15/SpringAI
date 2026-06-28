package com.spring.ai.springai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AIServiceTests {

    @Autowired
    private AiService aiService;

    @Autowired
    private RAGService ragService;

    @Test
    public void testGetJokes() {
        System.out.println(aiService.getJoke("developers"));
    }

}
