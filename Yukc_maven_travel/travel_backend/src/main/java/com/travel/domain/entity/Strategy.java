package com.travel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/*
 * 1.自动生成 get、set、toString 等方法，不用手动写
 * 2.告诉 MyBatis-Plus，这个类对应数据库里名叫 strategy 的表就算类名和表名字英文一样，显式写出来是企业习惯，不容易出错
 */

/**
 * 旅游攻略实体类
 * 对应数据库 strategy 表，存储各个景点、城市的攻略信息
 */
@Data
@NoArgsConstructor
@TableName("travel_strategy")
public class Strategy {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 攻略标题
     */
    private String title;
    /**
     * 所属城市名称
     */
    private String cityName;

    /**
     * 景点名称
     */
    @TableField("spot_name")
    //数据库字段名 和 Java 属性名 没法用驼峰转换对应（比如字段叫`s_name`，属性叫`spotName`）
    private String spotName;

    /**
     * 攻略正文内容
     */
    private String content;

    /**
     * 图片外链地址
     */
    private String imgUrl;

    /**
     * 标签，例如：免费、热门
     */
    private String tag;

    /**
     * 内容分类：历史古迹、自然风光、美食打卡
     */
    private String category;

    /**
     * 访问量
     */
    private Integer viewCount;

    /**
     * 点赞总数
     */
    private Integer likeCount;
    /**
     * 收藏总数
     */
    private Integer collectCount;

    /**
     * 收藏总数
     */
    private Integer commentCount;
    /**
     * 发布者用户ID
     */
    @TableField(value = "author_id")
    private Long authorId;

    /**
     * 状态 1=上线 0=下线
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
