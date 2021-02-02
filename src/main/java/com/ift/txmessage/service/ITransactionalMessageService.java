package com.ift.txmessage.service;

import com.ift.txmessage.support.binding.Destination;
import com.ift.txmessage.support.message.TxMessage;

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
     * @param txMessage 消息信息
     */
    void sendTransactionalMessage(Destination destination, TxMessage txMessage);
}
