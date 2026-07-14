package com.xiong.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiong.common.result.Result;
import com.xiong.note.client.RagClient;
import com.xiong.note.entity.Note;
import com.xiong.note.mapper.NoteMapper;
import com.xiong.note.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

    @Autowired
    private RagClient ragClient; // OpenFeign 客户端

    // 🌟 核心技巧：延迟注入自身代理对象，解决 @Async 在同类中调用失效的问题
    @Lazy
    @Autowired
    private NoteService noteService;

    @Override
    public Result<String> saveNote(Note note) {
        if (note.getTitle() == null || note.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }

        if (note.getContent() == null || note.getContent().isBlank()) {
            return Result.error(400, "内容不能为空");
        }
        // 1. 初始化数据并保存到 MySQL
        note.setCreateTime(LocalDateTime.now());
        // 保存后，MyBatis-Plus 会自动将 MySQL 生成的主键 ID 回填到 note 对象中
        this.save(note);

        // 2. 异步调用大模型打标签 (必须通过 selfService 调用，不能用 this)
        noteService.generateTagsAsync(note.getId(), note.getContent());

        return Result.success("笔记保存成功！AI正在后台为您生成标签...");
    }

    /**
     * 该方法会在 Spring 的后台线程池中默默运行，绝对不会卡住前端的保存请求
     */
    @Async
    @Override
    public void generateTagsAsync(Long noteId, String content) {
        try {
            logger.info("开始为笔记 ID: {} 异步生成标签...", noteId);

            // 限制传给大模型的文本长度，防止 Token 爆炸（取前 800 字足矣）
            String textToAnalyze = content.length() > 800 ? content.substring(0, 800) : content;

            Map<String, String> request = new HashMap<>();
            request.put("text", textToAnalyze);

            // 3. 通过 OpenFeign 远程调用 rag-service
            Result<String> response = ragClient.extractTags(request);

            if (response.getCode() == 200) {
                // 4. 将大模型生成的标签更新回数据库
                Note updateNote = new Note();
                updateNote.setId(noteId);
                updateNote.setTags(response.getData()); // 假设返回的是 "Java, SpringBoot, 微服务"
                this.updateById(updateNote);

                logger.info("笔记 ID: {} 标签生成成功: {}", noteId, response.getData());
            } else {
                logger.error("大模型生成标签失败: {}", response.getMessage());
            }
        } catch (Exception e) {
            logger.error("调用大模型生成标签发生异常", e);
        }
    }
}
