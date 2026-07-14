package com.xiong.rag.config;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    /**
     * 流式聊天模型（SSE）
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {

        return OllamaStreamingChatModel.builder()
                .baseUrl("http://127.0.0.1:11434")
                .modelName("qwen3.5:4b")
                .temperature(0.1)
                .timeout(Duration.ofMinutes(10)) // 5分钟的超时等待时间！
                .logRequests(true)   // 打印发给大模型的完整 Prompt
                .logResponses(true)  // 打印大模型返回的原始 JSON
                .build();
    }

    /**
     * 普通聊天模型（一次性返回）
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {

        return OllamaChatModel.builder()
                .baseUrl("http://127.0.0.1:11434")
                .modelName("qwen3.5:4b")
                .temperature(0.1)
                .timeout(Duration.ofMinutes(5))
                .build();
    }

    /**
     * 向量模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {

        return OllamaEmbeddingModel.builder()
                .baseUrl("http://127.0.0.1:11434")
                .modelName("nomic-embed-text")
                .build();
    }

    /**
     * Chroma 向量数据库
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {

        return ChromaEmbeddingStore.builder()
                .baseUrl("http://127.0.0.1:8085")
                .collectionName("xiong_rag_collection")
                .build();
    }

    /**
     * 文档写入器
     */
    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {

        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(800, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * RAG 检索器
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(6)
                .minScore(0.35)
                .build();
    }

    /**
     * 会话记忆
     */
    @Bean
    public MessageWindowChatMemory chatMemory() {

        return MessageWindowChatMemory.withMaxMessages(10);
    }

}
