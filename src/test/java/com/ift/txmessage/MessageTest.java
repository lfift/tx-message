package com.ift.txmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ift.txmessage.service.ITransactionalMessageService;
import com.ift.txmessage.support.binding.DefaultDestination;
import com.ift.txmessage.support.binding.ExchangeType;
import com.ift.txmessage.support.message.DefaultMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 事务消息测试
 *
 * @author liufei
 * @date 2021/2/2 17:09
 */
@Slf4j
@ActiveProfiles("dev")
@SpringBootTest
public class MessageTest {
    @Autowired
    private ITransactionalMessageService transactionalMessageService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(false)
    public void saveOrder() throws Exception {
        String orderId = UUID.randomUUID().toString();
        BigDecimal amount = BigDecimal.valueOf(100L);
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("amount", amount);
        String content = objectMapper.writeValueAsString(message);
        transactionalMessageService.sendTransactionalMessage(
                DefaultDestination.builder()
                        .exchangeName("tm.test.exchange")
                        .queueName("tm.test.queue")
                        .routingKey("tm.test.key")
                        .exchangeType(ExchangeType.DIRECT)
                        .build(),
                DefaultMessage.builder()
                        .messageId("123")
                        .businessKey(orderId)
                        .businessModule("SAVE_ORDER")
                        .content(content)
                        .build()
        );
        log.info("保存订单:{}成功...", orderId);
    }
}
