package com.ift.txmessage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 事务消息内容表
 * </p>
 *
 * @author liufei
 * @since 2021-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tx_message_content")
public class TransactionalMessageContent implements Serializable {

    private static final long serialVersionUID = -9191833216631490246L;
    /**
     * 主键
     */
    @TableId("id")
    private String id;

    /**
     * 事务消息记录ID
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 新增人
     */
    @TableField("create_user")
    private String createUser;

    /**
     * 新增时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField("update_user")
    private String updateUser;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;


}
