package com.xiong.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    // 定义一个 AI 助手接口，AiServices 会在底层动态代理它
    interface Assistant {
        @SystemMessage("你是一个专业的智能助手。请严格根据用户提供的上下文知识来回答问题。如果知识库中没有提及，请直接回答“我的知识库中没有相关信息”，绝不要瞎编。")
        String chat(String userMessage);
    }

    private final Assistant assistant;
    private final EmbeddingStoreIngestor ingestor;

    public ChatService() {
        // 1. 初始化对话大模型 (负责说话，保持 qwen3.5:4b)
        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl("http://127.0.0.1:11434")
                .modelName("qwen3.5:4b")
                .temperature(0.3) // 严谨模式，调低发散性
                .build();

        // 2. 初始化嵌入模型 (负责阅读理解，使用你拉取的 nomic-embed-text)
        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl("http://127.0.0.1:11434")
                .modelName("nomic-embed-text")
                .build();

        // 3. 连接 ChromaDB 向量数据库 (我们在 Docker 里映射的 8000 端口)
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl("http://127.0.0.1:8085")
                .collectionName("xiong_rag_collection")
                .build();

        // 4. 构建数据注入器 (负责把长文章切成一小段，转成向量存进数据库)
        this.ingestor = EmbeddingStoreIngestor.builder()
                // 核心：把超长文本按 300 个字符切片，段落间重叠 30 个字符防止语义断裂
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // 5. 构建内容检索器 (负责在用户提问时，去数据库里捞出最相关的 3 段知识)
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5) // 相似度及格线
                .build();

        // 6. 终极组装：把 大模型 + 检索器 + 历史记忆 全都塞给这个智能助手
        this.assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // 赋予它记忆最近 10 句话的能力
                .build();
    }

    /**
     * 向大模型喂入新的私有知识
     */
    public void feedKnowledge(String text) {
        Document doc = Document.from(text);
        ingestor.ingest(doc);
    }

    /**
     * 和 RAG 助手对话
     */
    public String chat(String question) {
        return assistant.chat(question);
    }
}
