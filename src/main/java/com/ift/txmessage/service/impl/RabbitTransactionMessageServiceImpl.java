package com.ift.txmessage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ift.txmessage.core.TxMessageManagementService;
import com.ift.txmessage.entity.TxMessage;
import com.ift.txmessage.mapper.TxMessageMapper;
import com.ift.txmessage.service.ITransactionalMessageService;
import com.ift.txmessage.support.binding.Destination;
import com.ift.txmessage.support.binding.ExchangeType;
import com.ift.txmessage.support.message.Message;
import com.ift.txmessage.support.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 事务消息RabbitMQ实现
 *
 * @author liufei
 * @date 2021/2/2 10:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitTransactionMessageServiceImpl implements ITransactionalMessageService {

    private final AmqpAdmin amqpAdmin;
    private final TxMessageManagementService managementService;
    private final TxMessageMapper txMessageMapper;

    private static final ConcurrentMap<String, Boolean> QUEUE_ALREADY_DECLARE = new ConcurrentHashMap<>();

    /**
     * 发送事务消息
     *
     * @param destination 消息队列目标信息
     * @param message   消息信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTransactionalMessage(Destination destination, Message message) {
        String queueName = destination.queueName();
        String exchangeName = destination.exchangeName();
        String routingKey = destination.routingKey();
        ExchangeType exchangeType = destination.exchangeType();
        // 原子性的预声明
        QUEUE_ALREADY_DECLARE.computeIfAbsent(queueName, k -> {
            Queue queue = new Queue(queueName);
            amqpAdmin.declareQueue(queue);
            Exchange exchange = new CustomExchange(exchangeName, exchangeType.getType());
            amqpAdmin.declareExchange(exchange);
            //TODO 是否需要声明其他参数
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
            amqpAdmin.declareBinding(binding);
            return true;
        });
        TxMessage record = new TxMessage();
        record.setMessageId(message.messageId());
        record.setQueueName(queueName);
        record.setExchangeName(exchangeName);
        record.setExchangeType(exchangeType.getType());
        record.setRoutingKey(routingKey);
        record.setBusinessModule(message.businessModule());
        record.setBusinessKey(message.businessKey());
        record.setDeleted("0");
        record.setCreateTime(LocalDateTime.now());
        record.setCreateUser("1");
        record.setUpdateTime(LocalDateTime.now());
        record.setUpdateUser("1");
        String content = message.content();
        // 保存事务消息记录
        managementService.saveTransactionalMessageRecord(record, content);
        // 注册事务同步器
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                managementService.sendMessageSync(record, content);
            }
        });
    }

    /**
     * 幂等性校验
     *
     * @param correlationId 消息唯一标识
     * @return true：通过，false：未通过
     */
    @Override
    public boolean checkIdempotent(String correlationId) {
        List<TxMessage> txMessages =
                txMessageMapper.selectList(Wrappers.<TxMessage>lambdaQuery()
                        .eq(TxMessage::getMessageId, correlationId)
                        .eq(TxMessage::getMessageStatus, MessageStatus.SUCCESS));
        return txMessages.isEmpty();
    }
}
