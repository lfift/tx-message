package com.ift.txmessage.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 事务消息表
 * </p>
 *
 * @author liufei
 * @since 2021-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tx_message")
public class TxMessage implements Serializable {

    private static final long serialVersionUID = -2381304812547174133L;
    /**
     * 主键
     */
    @TableId("message_id")
    private String messageId;

    /**
     * 队列名
     */
    @TableField("queue_name")
    private String queueName;

    /**
     * 交换器名
     */
    @TableField("exchange_name")
    private String exchangeName;

    /**
     * 交换器类型
     */
    @TableField("exchange_type")
    private String exchangeType;

    /**
     * 路由健
     */
    @TableField("routing_key")
    private String routingKey;

    /**
     * 业务模块
     */
    @TableField("business_module")
    private String businessModule;

    /**
     * 业务键
     */
    @TableField("business_key")
    private String businessKey;

    /**
     * 当前重试次数
     */
    @TableField("current_retry_times")
    private int currentRetryTimes;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_times")
    private int maxRetryTimes;

    /**
     * 下一次调度时间
     */
    @TableField(value = "next_schedule_time")
    private LocalDateTime nextScheduleTime;

    /**
     * 退避初始化值
     */
    @TableField("init_backoff")
    private int initBackoff;

    /**
     * 退避初始化值，单位为秒
     */
    @TableField("backoff_factor")
    private int backoffFactor;

    /**
     * 消息状态，0：待推送，1：推送成功，2：推送失败
     */
    @TableField("message_status")
    private int messageStatus;

    /**
     * 是否删除，1：是，0：否
     */
    @TableField(value = "deleted")
    @TableLogic
    private String deleted;

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
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;


}
