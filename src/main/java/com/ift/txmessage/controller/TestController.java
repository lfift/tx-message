package com.ift.txmessage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ift.txmessage.service.ITransactionalMessageService;
import com.ift.txmessage.support.binding.DefaultDestination;
import com.ift.txmessage.support.binding.ExchangeType;
import com.ift.txmessage.support.message.DefaultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author liufei
 * @date 2021/2/3 16:56
 */
@Slf4j
@RestController
public class TestController {
    @Autowired
    private ITransactionalMessageService transactionalMessageService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/sendMessage")
    public String sendMessage() throws JsonProcessingException {
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
                        .messageId(orderId)
                        .businessKey(orderId)
                        .businessModule("SAVE_ORDER")
                        .content(content)
                        .build()
        );
        log.info("保存订单:{}成功...", orderId);
        return "发送成功！";
    }
}
