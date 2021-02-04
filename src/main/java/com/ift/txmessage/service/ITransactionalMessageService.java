package com.ift.txmessage.service;

import com.ift.txmessage.support.binding.Destination;
import com.ift.txmessage.support.message.Message;

/**
 * 事务消息服务接口
 *
 * @author liufei
 * @date 2021/2/2 9:57
 */
public interface ITransactionalMessageService {

    /**
     * 发送事务消息
     *
     * @param destination 消息队列目标信息
     * @param message 消息信息
     */
    void sendTransactionalMessage(Destination destination, Message message);

    /**
     * 幂等性校验
     *
     * @param correlationId 消息唯一标识
     * @return true：通过，false：未通过
     */
    boolean checkIdempotent(String correlationId);
}
