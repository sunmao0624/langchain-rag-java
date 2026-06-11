package com.xiong.rag.controller;

import com.xiong.common.result.Result;
import com.xiong.rag.dto.ChatRequestDTO;
import com.xiong.rag.dto.KnowledgeDTO;
import com.xiong.rag.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequestDTO dto){
        String answer = chatService.chat(dto.getQuestion());
        return Result.success(answer);
    }

    @PostMapping("/feed")
    public Result<String> feed(@RequestBody KnowledgeDTO dto) {
        chatService.feedKnowledge(dto.getContent());
        return Result.success("知识录入成功！大模型已记住该信息。");
    }
}
