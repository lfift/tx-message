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
@TableName("TX_MESSAGE_CONTENT")
public class TransactionalMessageContent implements Serializable {

    private static final long serialVersionUID = -9191833216631490246L;
    /**
     * 主键
     */
    @TableId("ID")
    private String id;

    /**
     * 事务消息记录ID
     */
    @TableField("MESSAGE_ID")
    private String messageId;

    /**
     * 消息内容
     */
    @TableField("CONTENT")
    private String content;

    /**
     * 新增人
     */
    @TableField("CREATE_USER")
    private String createUser;

    /**
     * 新增时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField("UPDATE_USER")
    private String updateUser;

    /**
     * 修改时间
     */
    @TableField(value = "UPDATE_TIME", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;


}
