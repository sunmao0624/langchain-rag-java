package com.xiong.rag.dto;

import lombok.Data;

@Data
public class KnowledgeDTO {

    private String content;  // 我们要喂给大模型的私有知识文本
}
