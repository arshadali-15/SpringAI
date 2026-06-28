package com.spring.ai.springai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RAGService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    @Value("classpath:faq.pdf")
    Resource faqPDF;

    public float[] getEmbedding(String text) {
        return embeddingModel.embed(text);
    }

    public String askAIWithAdvisors(String prompt, String userId) {
        return chatClient.prompt()
                .system("""
                        You are an AI Assistant called cody.
                        Greet users wiht your name and the user name if you know their name.
                        Answer in a friendly manner.
                        """)
                .user(prompt)
                .advisors(
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        VectorStoreChatMemoryAdvisor
                                .builder(vectorStore)
                                .defaultTopK(3)
                                .build()
                ).advisors(
                        advisorSpec -> advisorSpec
                                .param(ChatMemory.CONVERSATION_ID, userId)
                )
                .call()
                .content();
    }

    public String askAI(String prompt) {

        String template = """
                Act as AI assistant helping a developer.
                Rules:
                    - use only the info provided in the context.
                    - do not introduce new facts or concepts.
                    - if answer is not present say "I dont know"
                CONTEXT :
                {context}
                
                Answer in a polite friendly manner.
                """;


        List<Document> documents = vectorStore.similaritySearch(SearchRequest
                .builder()
                .query(prompt)
                .topK(2)
                .similarityThreshold(0.5)
                .filterExpression("file_name == 'faq.pdf'")
                .build());

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        PromptTemplate promptTemplate = new PromptTemplate(template);
        String systemPrompt = promptTemplate.render(Map.of("context", context));

        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .advisors(
                        new SimpleLoggerAdvisor()
                )
                .call()
                .content();
    }

    public void ingestPdfToVectorStore() {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(faqPDF);
        List<Document> pages = reader.get();

        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(100)
                .build();

        List<Document> chunks = tokenTextSplitter.apply(pages);

        vectorStore.add(chunks);
    }

    public void ingestDataToVectorStore() {
        List<Document> movies = List.of(
                new Document(
                        "Inception is a science fiction thriller where a skilled thief enters people's dreams to steal secrets. He is given a chance to erase his criminal history by planting an idea into someone's subconscious.",
                        Map.of(
                                "title", "Inception",
                                "genre", "Sci-Fi",
                                "year", "2010",
                                "director", "Christopher Nolan"
                        )
                ),
                new Document(
                        "The Dark Knight follows Batman as he battles the Joker, a criminal mastermind determined to create chaos in Gotham City.",
                        Map.of(
                                "title", "The Dark Knight",
                                "genre", "Action",
                                "year", "2008",
                                "director", "Christopher Nolan"
                        )
                ),
                new Document(
                        "Interstellar tells the story of astronauts who travel through a wormhole in search of a new home for humanity as Earth becomes uninhabitable.",
                        Map.of(
                                "title", "Interstellar",
                                "genre", "Sci-Fi",
                                "year", "2014",
                                "director", "Christopher Nolan"
                        )
                ),
                new Document(
                        "The Shawshank Redemption is about a banker wrongly imprisoned for murder who forms an unlikely friendship and never loses hope.",
                        Map.of(
                                "title", "The Shawshank Redemption",
                                "genre", "Drama",
                                "year", "1994",
                                "director", "Frank Darabont"
                        )
                )
        );

        vectorStore.add(movies);
    }
}
