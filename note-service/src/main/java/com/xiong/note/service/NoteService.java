package com.xiong.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiong.common.result.Result;
import com.xiong.note.entity.Note;

public interface NoteService extends IService<Note> {
    // 自定义保存笔记的方法（包含触发 AI 逻辑）
    Result<String> saveNote(Note note);

    // 异步调用大模型打标签的方法
    void generateTagsAsync(Long noteId, String content);
}
