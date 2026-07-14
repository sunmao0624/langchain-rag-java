package com.xiong.note.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiong.common.result.Result;
import com.xiong.note.entity.Note;
import com.xiong.note.entity.ReviewTask;
import com.xiong.note.service.NoteService;
import com.xiong.note.service.ReviewTaskService;
import com.xiong.note.utils.UserContext;
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
        // 1. 去 review_task 表查出今天分配给该用户的所有未复习的任务 ID
        LambdaQueryWrapper<ReviewTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(ReviewTask::getUserId, UserContext.getUserId())
                .eq(ReviewTask::getReviewDate, LocalDate.now())
                .eq(ReviewTask::getIsReviewed, false);

        List<ReviewTask> tasks = reviewTaskService.list(taskWrapper);

        if (tasks.isEmpty()) {
            return Result.success(null); // 今天没有复习任务
        }

        // 2. 提取出所有需要复习的 noteId
        List<Long> noteIds = tasks.stream()
                .map(ReviewTask::getNoteId)
                .collect(Collectors.toList());

        // 3. 去 note 表把具体的笔记内容查出来返回给前端卡片
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
