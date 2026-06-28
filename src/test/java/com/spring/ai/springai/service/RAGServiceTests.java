package com.spring.ai.springai.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
public class RAGServiceTests {

    @Autowired
    private RAGService ragService;

    @Test
    public void testEmbedText() {
        var embed = ragService.getEmbedding("This is a test");
        for (float e : embed) {
            System.out.print(e + " ");
        }
    }

    @Test
    public void testStoreData() {
        ragService.ingestDataToVectorStore();
    }

    @Test
    public void testIngestPdfToVectorStore() {
        ragService.ingestPdfToVectorStore();

    }

    @Test
    public void testAskAI() {
        var response = ragService.askAI("Best car brand?");
        System.out.println(response);

    }

    @Test
    public void testAskAIWithAdvisors() {
        var response = ragService.askAIWithAdvisors("tell me the best car and my name!", "Arshad123");
        System.out.println(response);

    }
}
