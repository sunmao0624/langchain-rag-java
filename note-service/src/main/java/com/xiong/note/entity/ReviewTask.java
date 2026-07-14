package com.xiong.note.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("review_task")
public class ReviewTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long noteId;
    private LocalDate reviewDate; // 需要复习的日期
    private Boolean isReviewed;   // 是否已打卡复习
}
