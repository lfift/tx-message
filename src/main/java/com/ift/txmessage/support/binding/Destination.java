package com.ift.txmessage.support.binding;

/**
 * 目标
 *
 * @author liufei
 * @date 2021/2/2 10:07
 */
public interface Destination {

    /**
     * 交换器类型
     * @return 交换器类型
     */
    ExchangeType exchangeType();

    /**
     * 队列名称
     * @return 队列名称
     */
    String queueName();

    /**
     * 交换器名称
     * @return 交换器名称
     */
    String exchangeName();

    /**
     * 路由键
     * @return 路由键
     */
    String routingKey();

}
