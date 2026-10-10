package com.travel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("travel_comment")
public class TravelComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long strategyId;

    private String commentContent;

    /**
     * 父评论ID：0表示顶级评论，大于0表示回复某条评论
     * 用于实现"楼中楼"二级评论结构
     */
    private Long parentId;

    private LocalDateTime createTime;
}
