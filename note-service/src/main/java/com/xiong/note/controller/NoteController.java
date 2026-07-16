package com.xiong.note.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiong.common.result.Result;
import com.xiong.common.utils.UserContext;
import com.xiong.note.entity.Note;
import com.xiong.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    /**
     * 保存笔记 (触发异步打标签)
     */
    @PostMapping("/save")
    public Result<String> saveNote(@RequestBody Note note) {
        // 1. 绑定当前登录用户
        Long userId = UserContext.getUserId();
        note.setUserId(userId);

        // 2. 调用 Service 层保存并触发 AI 异步打标签
        return noteService.saveNote(note);
    }

    /**
     * 获取我的笔记列表
     */
    @GetMapping("/list")
    public Result<List<Note>> getMyNotes() {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Note::getUserId, UserContext.getUserId())
                .orderByDesc(Note::getCreateTime); // 按时间倒序

        List<Note> notes = noteService.list(queryWrapper);
        return Result.success(notes);
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    public Result<Note> getNoteDetail(@PathVariable Long id) {
        Note note = noteService.getById(id);

        // 越权校验：只能看自己的笔记
        if (note == null || !note.getUserId().equals(UserContext.getUserId())) {
            return Result.error(403, "无权访问或笔记不存在");
        }
        return Result.success(note);
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteNote(@PathVariable Long id) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getId, id).eq(Note::getUserId, UserContext.getUserId());

        boolean removed = noteService.remove(wrapper);
        return removed ? Result.success("删除成功") : Result.error(400, "删除失败");
    }
}
