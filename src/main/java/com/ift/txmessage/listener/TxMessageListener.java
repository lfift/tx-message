package com.ift.txmessage.listener;

import com.ift.txmessage.service.ITransactionalMessageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author liufei
 * @date 2021/2/3 16:46
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TxMessageListener {

    private final ITransactionalMessageService transactionalMessageService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "tm.test.queue", durable = "true",
            arguments = {
                    @Argument(name = "x-message-ttl", value = "10000", type = "java.lang.Long"),
                    @Argument(name = "x-dead-letter-exchange", value = "dlx.exchange")
            }),
            exchange = @Exchange(name = "tm.test.exchange", type = ExchangeTypes.DIRECT, durable = "true"),
            key = "tm.test.key"))
    public void txMessageListener(Message message, Channel channel)  {
        boolean idempotent = transactionalMessageService.checkIdempotent(message.getMessageProperties().getCorrelationId());
        if (!idempotent) {
            return;
        }
        log.info("message: {}", new String(message.getBody()));
        //使用此ID进行幂等性判断
        log.info("correlationId: {}", message.getMessageProperties().getCorrelationId());
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
