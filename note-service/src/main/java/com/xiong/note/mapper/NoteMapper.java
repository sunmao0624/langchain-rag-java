package com.xiong.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiong.note.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
