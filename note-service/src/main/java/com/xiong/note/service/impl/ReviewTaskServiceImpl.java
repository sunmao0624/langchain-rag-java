package com.xiong.note.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiong.note.entity.ReviewTask;
import com.xiong.note.mapper.ReviewTaskMapper;
import com.xiong.note.service.ReviewTaskService;
import org.springframework.stereotype.Service;

@Service
public class ReviewTaskServiceImpl extends ServiceImpl<ReviewTaskMapper, ReviewTask> implements ReviewTaskService{

}
