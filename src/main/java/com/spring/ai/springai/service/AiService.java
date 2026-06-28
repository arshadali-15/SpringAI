package com.spring.ai.springai.service;

import com.spring.ai.springai.dto.Joke;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;


    public List<Document> similaritySearch(String text) {
        return vectorStore.similaritySearch(SearchRequest
                .builder()
                .query(text)
                .topK(2)
                .similarityThreshold(0.4)
                .build());
    }

    public String getJoke(String topic) {

        String prompt = """
                Act as an excellent comedian with a sarcastic feel. Provide jokes on the {topic} in a single line
                """;
        PromptTemplate promptTemplate = new PromptTemplate(prompt);

        String renderText = promptTemplate.render(Map.of("topic", topic));

        var response = chatClient.prompt()
                .user(renderText)
                .advisors()
                .call()
                .entity(Joke.class);

        assert response != null;
        return response.text();
    }
}

