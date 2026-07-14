package com.xiong.rag.controller;

import com.xiong.common.result.Result;
import com.xiong.rag.client.UserClient;
import com.xiong.rag.dto.ChatRequestDTO;
import com.xiong.rag.dto.KnowledgeDTO;
import com.xiong.rag.service.ChatService;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserClient userClient; // <--- 注入 Feign 客户端

    @PostMapping("/feed")
    public Result<String> feed(@RequestBody KnowledgeDTO dto) {
        chatService.feedKnowledge(dto.getContent());
        return Result.success("知识录入成功！大模型已记住该信息。");
    }

    @PostMapping("/upload/pdf")
    public Result<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "上传的文件不能为空");
        }
        try {
            // 1. 先去解析并存储大模型知识库
            chatService.feedKnowledgeFromCloudAPI(file);

            // 2. 🌟 知识录入成功后，通过 OpenFeign 远程调用 user-service 扣除积分
            // 假设当前登录的用户 ID 是 1001，扣除 10 积分
            Result<String> feignResult = userClient.deductPoint(1001L, 10);

            System.out.println("远程调用 user-service 返回结果: " + feignResult.getMessage());

            return Result.success("PDF 知识解析成功！且已远程扣除 10 个积分。");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 大模型打字机流式输出接口
     * 注意：因为是长连接推送，返回值必须是 SseEmitter
     */
    @PostMapping(value = "/chat/stream")
    public SseEmitter streamChat(@RequestBody ChatRequestDTO dto) {
        // 1. 创建一个具有 3 分钟超时时间的 SSE 发射器
        SseEmitter emitter = new SseEmitter(180000L);

        // 2. 异步处理：当连接超时、错误或完成时的回调清理动作
        emitter.onCompletion(() -> System.out.println("SSE 连接正常结束"));
        emitter.onTimeout(() -> System.out.println("SSE 连接超时"));
        emitter.onError((ex) -> System.out.println("SSE 连接发生错误: " + ex.getMessage()));

        try {
            // 3. 激活大模型的 Token 管道
            TokenStream tokenStream = chatService.streamChat(dto.getQuestion());
            StringBuilder buffer = new StringBuilder();

            // 4. 将大模型的管道与 Spring 的 SSE 发射器进行高频“焊接”
            tokenStream
                    .onNext(token -> {
                        try {
                            // 拼接模型返回的 token
                            buffer.append(token);

                            // 遇到一句话结束时再发送
                            if (token.contains("。")
                                    || token.contains("！")
                                    || token.contains("？")
                                    || token.contains("\n")) {

                                emitter.send(SseEmitter.event().data(buffer.toString()));
                                buffer.setLength(0); // 清空缓冲区
                            }

                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .onComplete(response -> {
                        try {
                            // 把最后剩余的内容发出去
                            if (buffer.length() > 0) {
                                emitter.send(SseEmitter.event().data(buffer.toString()));
                            }
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                        emitter.complete();
                    })
                    .onError(emitter::completeWithError)
                    .start();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
            // 5. 立刻把发射器返回给 Spring MVC，此时 HTTP 响应头已被修改，长连接正式建立
            return emitter;
        }

    @PostMapping("/extract-tags")
    public Result<String> extractTags(@RequestBody Map<String, String> request) {

        String text = request.get("text");

        String tags = chatService.extractTags(text);

        return Result.success(tags);
    }
    }

