package com.ift.txmessage.listener;

import com.ift.txmessage.service.ITransactionalMessageService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author liufei
 * @date 2021/2/3 16:46
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TxMessageListener {

    private final ITransactionalMessageService transactionalMessageService;

    @RabbitListener(queues = "tm.test.queue", ackMode = "MANUAL")
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
