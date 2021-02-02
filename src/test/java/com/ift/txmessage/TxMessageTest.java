package com.ift.txmessage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ift.txmessage.service.ITransactionalMessageService;
import com.ift.txmessage.support.binding.DefaultDestination;
import com.ift.txmessage.support.binding.ExchangeType;
import com.ift.txmessage.support.message.DefaultTxMessage;
import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
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
@ActiveProfiles("home")
@SpringBootTest
public class TxMessageTest {
    @Autowired
    private ITransactionalMessageService transactionalMessageService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder() throws Exception {
        String orderId = UUID.randomUUID().toString();
        BigDecimal amount = BigDecimal.valueOf(100L);
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("amount", amount);
        String content = objectMapper.writeValueAsString(message);
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};
        Map<String, Object> map = objectMapper.readValue("{\"code\":200,\"message\":\"123\"}", typeReference);
        System.out.println(map);
        transactionalMessageService.sendTransactionalMessage(
                DefaultDestination.builder()
                        .exchangeName("tm.test.exchange")
                        .queueName("tm.test.queue")
                        .routingKey("tm.test.key")
                        .exchangeType(ExchangeType.DIRECT)
                        .build(),
                DefaultTxMessage.builder()
                        .businessKey(orderId)
                        .businessModule("SAVE_ORDER")
                        .content(content)
                        .build()
        );
        log.info("保存订单:{}成功...", orderId);
    }
}
