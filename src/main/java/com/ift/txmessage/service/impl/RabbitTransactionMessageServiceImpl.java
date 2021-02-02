package com.ift.txmessage.service.impl;

import com.ift.txmessage.core.TxMessageManagementService;
import com.ift.txmessage.entity.TransactionalMessage;
import com.ift.txmessage.service.ITransactionalMessageService;
import com.ift.txmessage.support.binding.Destination;
import com.ift.txmessage.support.binding.ExchangeType;
import com.ift.txmessage.support.message.TxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;
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

    private static final ConcurrentMap<String, Boolean> QUEUE_ALREADY_DECLARE = new ConcurrentHashMap<>();

    /**
     * 发送事务消息
     *
     * @param destination 消息队列目标信息
     * @param txMessage   消息信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTransactionalMessage(Destination destination, TxMessage txMessage) {
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
        TransactionalMessage record = new TransactionalMessage();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setQueueName(queueName);
        record.setExchangeName(exchangeName);
        record.setExchangeType(exchangeType.getType());
        record.setRoutingKey(routingKey);
        record.setBusinessModule(txMessage.businessModule());
        record.setBusinessKey(txMessage.businessKey());
        record.setDeleted("0");
        record.setCreateTime(LocalDateTime.now());
        record.setCreateUser("1");
        record.setUpdateTime(LocalDateTime.now());
        record.setUpdateUser("1");
        String content = txMessage.content();
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
}
