package com.Back.product.service;

import com.Back.product.tool.ProductSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ProductChatService {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ProductSearchTool productSearchTool;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
        당신은 친절한 상품 추천 어시스턴트입니다.
        사용자의 요구사항과 선호도에 맞는 상품을 찾는 것을 도와줍니다.
        
        사용자가 상품에 대해 질문하면, 사용 가능한 도구를 사용하여 상품을 검색하세요:
        - searchProducts: 키워드로 상품 검색
        - findSimilarProducts: 특정 상품과 유사한 상품 찾기
        - getProductById: 특정 상품의 상세 정보 조회
        - getAllProducts: 모든 상품 목록 조회
        
        검색 결과를 바탕으로 항상 도움이 되는 추천을 제공하세요.
        상품을 찾지 못한 경우, 사용자에게 알리고 대안적인 검색어를 제안하세요.
        
        이전 대화 내용을 기억하고 맥락에 맞는 응답을 제공하세요.
        친절하고 도움이 되는 방식으로 응답하세요.
        """;

    public ChatResponse chat(String userMessage, String userId) {
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(productSearchTool)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .call()
                .content();

        return new ChatResponse(response);
    }

    public record ChatResponse(String message) {}

    public Flux<String> chatStream(String userMessage, String userId) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .tools(productSearchTool)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        VectorStoreChatMemoryAdvisor.builder(vectorStore).build()
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .stream()
                .content();
    }
}

