package com.xiong.note.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiong.note.entity.ReviewTask;

import java.util.List;

public interface ReviewTaskService extends IService<ReviewTask> {

    List<ReviewTask> getTodayTasks(Long userId);

    /**
     * 生成艾宾浩斯复习任务
     */
    void generateEbbinghausTasks(Long noteId, Long userId);
}
