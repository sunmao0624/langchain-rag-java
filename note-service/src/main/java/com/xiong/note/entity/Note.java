package com.xiong.note.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;     // 从网关透传的 Header 中获取
    private String title;
    private String content;  // Markdown 原文
    private String tags;     // AI 自动生成的标签，逗号分隔
    private LocalDateTime createTime;
}
