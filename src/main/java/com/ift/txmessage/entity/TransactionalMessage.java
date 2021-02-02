package com.ift.txmessage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LocalDateTimeTypeHandler;

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
@TableName("TX_MESSAGE")
public class TransactionalMessage implements Serializable {

    private static final long serialVersionUID = -2381304812547174133L;
    /**
     * 主键
     */
    @TableId("ID")
    private String id;

    /**
     * 队列名
     */
    @TableField("QUEUE_NAME")
    private String queueName;

    /**
     * 交换器名
     */
    @TableField("EXCHANGE_NAME")
    private String exchangeName;

    /**
     * 交换器类型
     */
    @TableField("EXCHANGE_TYPE")
    private String exchangeType;

    /**
     * 路由健
     */
    @TableField("ROUTING_KEY")
    private String routingKey;

    /**
     * 业务模块
     */
    @TableField("BUSINESS_MODULE")
    private String businessModule;

    /**
     * 业务键
     */
    @TableField("BUSINESS_KEY")
    private String businessKey;

    /**
     * 当前重试次数
     */
    @TableField("CURRENT_RETRY_TIMES")
    private int currentRetryTimes;

    /**
     * 最大重试次数
     */
    @TableField("MAX_RETRY_TIMES")
    private int maxRetryTimes;

    /**
     * 下一次调度时间
     */
    @TableField(value = "NEXT_SCHEDULE_TIME", jdbcType = JdbcType.DATE, typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime nextScheduleTime;

    /**
     * 退避初始化值
     */
    @TableField("INIT_BACKOFF")
    private int initBackoff;

    /**
     * 退避初始化值，单位为秒
     */
    @TableField("BACKOFF_FACTOR")
    private int backoffFactor;

    /**
     * 消息状态，0：待推送，1：推送成功，2：推送失败
     */
    @TableField("MESSAGE_STATUS")
    private int messageStatus;

    /**
     * 是否删除，1：是，0：否
     */
    @TableField(value = "DELETED", fill = FieldFill.INSERT)
    @TableLogic
    private String deleted;

    /**
     * 新增人
     */
    @TableField("CREATE_USER")
    private String createUser;

    /**
     * 新增时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT, typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField("UPDATE_USER")
    private String updateUser;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_TIME", fill = FieldFill.INSERT, typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime updateTime;


}
