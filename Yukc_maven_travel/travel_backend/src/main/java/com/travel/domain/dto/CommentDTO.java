package com.travel.domain.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long strategyId;
    private String content;
    /** 父评论ID，0或不传表示顶级评论 */
    private Long parentId;
}
