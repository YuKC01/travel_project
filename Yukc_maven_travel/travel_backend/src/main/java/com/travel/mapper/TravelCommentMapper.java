package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.domain.entity.TravelComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TravelCommentMapper extends BaseMapper<TravelComment> {
}
