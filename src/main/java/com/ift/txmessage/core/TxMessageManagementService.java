package com.ift.txmessage.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ift.txmessage.entity.TransactionalMessage;
import com.ift.txmessage.entity.TransactionalMessageContent;
import com.ift.txmessage.mapper.TxMessageContentMapper;
import com.ift.txmessage.mapper.TxMessageMapper;
import com.ift.txmessage.support.message.TxMessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 事务消息管理业务类
 *
 * @author liufei
 * @date 2021/2/2 10:46
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TxMessageManagementService {
    private final TxMessageMapper txMessageMapper;
    private final TxMessageContentMapper txMessageContentMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final LocalDateTime END =
            LocalDateTime.of(2999, 1, 1, 0, 0, 0);
    private static final int DEFAULT_INIT_BACKOFF = 10;
    private static final int DEFAULT_BACKOFF_FACTOR = 2;
    private static final int DEFAULT_MAX_RETRY_TIMES = 5;
    private static final int SIZE = 100;

    /**
     * 保存消息记录
     *
     * @param transactionalMessage 消息信息
     * @param content   消息内容
     */
    public void saveTransactionalMessageRecord(TransactionalMessage transactionalMessage, String content) {
        transactionalMessage.setMessageStatus(TxMessageStatus.PENDING.getStatus());
        transactionalMessage.setNextScheduleTime(calculateNextScheduleTime(LocalDateTime.now(), DEFAULT_INIT_BACKOFF,
                DEFAULT_BACKOFF_FACTOR, 0));
        transactionalMessage.setCurrentRetryTimes(0);
        transactionalMessage.setInitBackoff(DEFAULT_INIT_BACKOFF);
        transactionalMessage.setBackoffFactor(DEFAULT_BACKOFF_FACTOR);
        transactionalMessage.setMaxRetryTimes(DEFAULT_MAX_RETRY_TIMES);
        txMessageMapper.insert(transactionalMessage);
        TransactionalMessageContent transactionalMessageContent = new TransactionalMessageContent();
        transactionalMessageContent.setId(UUID.randomUUID().toString().replace("-", ""));
        transactionalMessageContent.setContent(content);
        transactionalMessageContent.setMessageId(transactionalMessage.getId());
        transactionalMessageContent.setCreateUser(transactionalMessage.getCreateUser());
        transactionalMessageContent.setCreateTime(transactionalMessage.getCreateTime());
        transactionalMessageContent.setUpdateUser(transactionalMessage.getUpdateUser());
        transactionalMessageContent.setUpdateTime(transactionalMessage.getUpdateTime());
        txMessageContentMapper.insert(transactionalMessageContent);
    }

    /**
     * 发送消息
     *
     * @param transactionalMessage 消息信息
     * @param content   消息内容
     */
    public void sendMessageSync(TransactionalMessage transactionalMessage, String content) {
        try {
            rabbitTemplate.convertAndSend(transactionalMessage.getExchangeName(), transactionalMessage.getRoutingKey(), content);
            if (log.isDebugEnabled()) {
                log.debug("发送消息成功,目标队列:{},消息内容:{}", transactionalMessage.getQueueName(), content);
            }
            // 标记成功
            markSuccess(transactionalMessage);
        } catch (Exception e) {
            // 标记失败
            markFail(transactionalMessage, e);
        }
    }

    /**
     * 标记成功
     *
     * @param transactionalMessage 消息信息
     */
    private void markSuccess(TransactionalMessage transactionalMessage) {
        //设置下次重推时间为最大值
        transactionalMessage.setNextScheduleTime(END);
        if (transactionalMessage.getCurrentRetryTimes() < transactionalMessage.getMaxRetryTimes()) {
            transactionalMessage.setCurrentRetryTimes(transactionalMessage.getCurrentRetryTimes() + 1);
        }
        transactionalMessage.setMessageStatus(TxMessageStatus.SUCCESS.getStatus());
        transactionalMessage.setUpdateTime(LocalDateTime.now());
        txMessageMapper.updateById(transactionalMessage);
    }

    /**
     * 标记失败
     *
     * @param transactionalMessage 消息信息
     * @param e 异常对象
     */
    private void markFail(TransactionalMessage transactionalMessage, Exception e) {
        log.error("发送消息失败,目标队列:{}", transactionalMessage.getQueueName(), e);
        if (transactionalMessage.getCurrentRetryTimes() < transactionalMessage.getMaxRetryTimes()) {
            transactionalMessage.setCurrentRetryTimes(transactionalMessage.getCurrentRetryTimes() + 1);
        }
        // 计算下一次的执行时间
        LocalDateTime nextScheduleTime = calculateNextScheduleTime(
                transactionalMessage.getNextScheduleTime(),
                transactionalMessage.getInitBackoff(),
                transactionalMessage.getBackoffFactor(),
                transactionalMessage.getCurrentRetryTimes()
        );
        transactionalMessage.setNextScheduleTime(nextScheduleTime);
        transactionalMessage.setMessageStatus(TxMessageStatus.FAIL.getStatus());
        transactionalMessage.setUpdateTime(LocalDateTime.now());
        txMessageMapper.updateById(transactionalMessage);
    }

    /**
     * 计算下一次执行时间
     *
     * @param base          基础时间
     * @param initBackoff   退避基准值
     * @param backoffFactor 退避指数
     * @param round         轮数
     * @return LocalDateTime
     */
    private LocalDateTime calculateNextScheduleTime(LocalDateTime base,
                                                    long initBackoff,
                                                    long backoffFactor,
                                                    long round) {
        double delta = initBackoff * Math.pow(backoffFactor, round);
        return base.plusSeconds((long) delta);
    }

    /**
     * 推送补偿 - 里面的参数应该根据实际场景定制
     */
    public void processPendingCompensationRecords() {
        // 这里预防把刚保存的消息也推送了
        LocalDateTime max = LocalDateTime.now().plusSeconds(-DEFAULT_INIT_BACKOFF);
        LocalDateTime min = max.plusHours(-1);
        LambdaQueryWrapper<TransactionalMessage> messageQueryWrapper = Wrappers.lambdaQuery();
        messageQueryWrapper.between(TransactionalMessage::getNextScheduleTime, min, max);
        Page<TransactionalMessage> page = new Page<>();
        page.setSize(SIZE);
        page.setPages(1);
        Map<String, TransactionalMessage> messages =
                txMessageMapper.selectPage(page, messageQueryWrapper).getRecords()
                        .stream().collect(Collectors.toMap(TransactionalMessage::getId, x -> x));
        if (messages.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<TransactionalMessageContent> messageContentQueryWrapper =
                Wrappers.lambdaQuery();
        messageContentQueryWrapper.in(TransactionalMessageContent::getMessageId, messages.keySet());
        txMessageContentMapper.selectList(messageContentQueryWrapper).forEach(item -> {
            TransactionalMessage transactionalMessage = messages.get(item.getMessageId());
            sendMessageSync(transactionalMessage, item.getContent());
        });
    }
}
