package com.xiong.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    /**
     * RAG 对话助手
     */
    private interface Assistant {
        @SystemMessage("""
            你是一名专业的论文问答助手。
            请遵循以下规则：
            【回答原则】
            1. 优先回答用户真正的问题，不要复述整篇论文。
            2. 不要出现：
            - 根据上下文
            - 根据提供的信息
            - 根据知识库
            - 文档中提到
            直接回答即可。
            3. 如果知识库可以回答：
            直接组织语言回答。
            4. 如果答案分散在多个段落：
            请综合多个片段后回答。
            5. 如果知识库没有答案：
            明确回答：
            "知识库中没有相关信息。"
            不要猜测。
            【回答风格】
            语言自然。
            像 ChatGPT。
            不要机械列：
            研究背景
            研究目的
            核心贡献
            除非用户要求。
            回答尽量简洁准确。
        """)
        TokenStream chat(String userMessage);
    }

    /**
     * 标签提取助手
     */
    private interface TagAssistant {

        @SystemMessage("""
                你是一名文本标签提取助手。
                请遵守下面规则：
                1、根据文本提取3~5个最核心标签
                2、不要解释
                3、不要编号
                4、不要输出其它内容
                5、标签之间使用英文逗号
                示例：
                输入：
                今天学习 SpringBoot、Redis、MyBatis。
                输出：
                SpringBoot,Redis,MyBatis
                """)
        String chat(String text);
    }

    private final Assistant assistant;
    private final TagAssistant tagAssistant;
    private final EmbeddingStoreIngestor ingestor;

    public ChatService(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatLanguageModel chatLanguageModel,
            ContentRetriever contentRetriever,
            EmbeddingStoreIngestor ingestor,
            MessageWindowChatMemory chatMemory) {

        this.ingestor = ingestor;

        /*
          RAG 助手
         */
        this.assistant = AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .build();

        /*
          标签助手
         */
        this.tagAssistant = AiServices.builder(TagAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 写入知识库
     */
    public void feedKnowledge(String text) {

        log.info("开始录入知识，文本长度：{}", text.length());

        Document document = Document.from(text);

        ingestor.ingest(document);

        log.info("知识录入完成");
    }

    /**
     * PDF 导入
     */
    public void feedKnowledgeFromCloudAPI(MultipartFile file)
            throws IOException {
        log.info("开始解析 PDF：{}", file.getOriginalFilename());

        ApachePdfBoxDocumentParser parser =
                new ApachePdfBoxDocumentParser();

        Document document =
                parser.parse(file.getInputStream());

        log.info("PDF 解析完成");

        ingestor.ingest(document);

        log.info("PDF 已成功写入知识库");
    }

    /**
     * 流式聊天
     */
    public TokenStream streamChat(String question) {
        String q = question.length() > 100
                ? question.substring(0,100)
                : question;

        log.info("收到聊天请求：{}", q);

        return assistant.chat(question);
    }

    /**
     * AI 标签提取
     */
    public String extractTags(String text) {

        log.info("开始提取标签");

        if (text == null || text.isBlank()) {
            return "";
        }

        // 防止 Token 过大
        if (text.length() > 800) {

            log.warn("文本过长，已截断，原长度：{}", text.length());

            text = text.substring(0, 800);
        }

        String tags = tagAssistant.chat(text);

        log.info("标签提取成功：{}", tags);

        return tags;
    }
}
