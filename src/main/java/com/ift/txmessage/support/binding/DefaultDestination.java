package com.ift.txmessage.support.binding;

import lombok.Builder;

/**
 * 消息队列目标默认实现
 *
 * @author liufei
 * @date 2021/2/2 10:11
 */
@Builder
public class DefaultDestination implements Destination {

    /**
     * 交换器类型
     */
    private ExchangeType exchangeType;
    /**
     * 队列名称
     */
    private String queueName;
    /**
     * 交换器名称
     */
    private String exchangeName;
    /**
     * 路由键
     */
    private String routingKey;

    /**
     * 交换器类型
     *
     * @return 交换器类型
     */
    @Override
    public ExchangeType exchangeType() {
        return this.exchangeType;
    }

    /**
     * 队列名称
     *
     * @return 队列名称
     */
    @Override
    public String queueName() {
        return this.queueName;
    }

    /**
     * 交换器名称
     *
     * @return 交换器名称
     */
    @Override
    public String exchangeName() {
        return this.exchangeName;
    }

    /**
     * 路由键
     *
     * @return 路由键
     */
    @Override
    public String routingKey() {
        return this.routingKey;
    }
}
