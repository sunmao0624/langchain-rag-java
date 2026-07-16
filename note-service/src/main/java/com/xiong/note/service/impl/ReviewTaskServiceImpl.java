package com.xiong.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiong.note.entity.ReviewTask;
import com.xiong.note.mapper.ReviewTaskMapper;
import com.xiong.note.service.ReviewTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewTaskServiceImpl extends ServiceImpl<ReviewTaskMapper, ReviewTask> implements ReviewTaskService{
    @Autowired
    public ReviewTaskService reviewTaskService;

    public List<ReviewTask> getTodayTasks(Long userId) {
        LocalDate today = LocalDate.now();
        // 使用 MyBatis-Plus 查询当前用户、且复习日期是今天、且状态为未复习的任务
        return this.lambdaQuery()
                .eq(ReviewTask::getUserId, userId)
                .le(ReviewTask::getReviewDate, today) // 小于等于今天（防止漏掉昨天的）
                .eq(ReviewTask::getIsReviewed, false)
                .list();
    }

    /**
     * 按照遗忘曲线生成复习任务
     */
    @Override
    public void generateEbbinghausTasks(Long noteId, Long userId) {

        int[] days = {1, 2, 4, 7, 15, 30};

        LocalDateTime now = LocalDateTime.now();

        List<ReviewTask> tasks = new ArrayList<>();

        for (int day : days) {

            ReviewTask task = new ReviewTask();

            task.setNoteId(noteId);
            task.setUserId(userId);
            task.setReviewDate(now.plusDays(day).toLocalDate());
            task.setIsReviewed(false);

            tasks.add(task);
        }

        this.saveBatch(tasks);
    }
}
