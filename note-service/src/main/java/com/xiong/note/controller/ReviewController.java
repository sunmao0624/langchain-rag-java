package com.xiong.note.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiong.common.result.Result;
import com.xiong.common.utils.UserContext;
import com.xiong.note.entity.Note;
import com.xiong.note.entity.ReviewTask;
import com.xiong.note.service.NoteService;
import com.xiong.note.service.ReviewTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/note/review")
public class ReviewController {
    @Autowired
    private ReviewTaskService reviewTaskService;
    @Autowired
    private NoteService noteService;

    /**
     * 获取今天需要复习的笔记列表
     */
    @GetMapping("/today")
    public Result<List<Note>> getTodayReviewTasks() {

        Long userId = UserContext.getUserId();

        List<ReviewTask> tasks = reviewTaskService.getTodayTasks(userId);

        if (tasks.isEmpty()) {
            return Result.success(null);
        }

        List<Long> noteIds = tasks.stream()
                .map(ReviewTask::getNoteId)
                .toList();

        List<Note> notesToReview = noteService.listByIds(noteIds);

        return Result.success(notesToReview);
    }

    /**
     * 标记某篇笔记已复习完 (打卡)
     */
    @PostMapping("/done/{noteId}")
    public Result<String> markAsReviewed(@PathVariable Long noteId) {
        LambdaQueryWrapper<ReviewTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewTask::getNoteId, noteId)
                .eq(ReviewTask::getUserId, UserContext.getUserId())
                .eq(ReviewTask::getReviewDate, LocalDate.now());

        ReviewTask task = reviewTaskService.getOne(wrapper);
        if (task != null) {
            task.setIsReviewed(true);
            reviewTaskService.updateById(task);
            return Result.success("打卡成功，记忆加深！");
        }
        return Result.error(400, "未找到该复习任务");
    }
}
