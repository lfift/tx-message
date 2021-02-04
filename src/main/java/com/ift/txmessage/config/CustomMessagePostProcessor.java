package com.ift.txmessage.config;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

/**
 * 自定义消息后置处理器
 *
 * @author liufei
 * @date 2021/2/3 17:37
 */
@Component
public class CustomMessagePostProcessor implements MessagePostProcessor {
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        return message;
    }

    @Override
    public Message postProcessMessage(Message message, Correlation correlation) {
        if (correlation instanceof CorrelationData) {
            CorrelationData correlationData = (CorrelationData) correlation;
            message.getMessageProperties().setCorrelationId(correlationData.getId());
        }
        return message;
    }
}
