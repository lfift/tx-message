package com.ift.txmessage.listener;

import com.rabbitmq.client.Channel;
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
public class TxMessageListener {

    @RabbitListener(queues = "tm.test.queue", ackMode = "MANUAL")
    public void txMessageListener(Message message, Channel channel)  {
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
