package com.travel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_collect")
public class UserCollect {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long strategyId;

    private LocalDateTime createTime;
}
