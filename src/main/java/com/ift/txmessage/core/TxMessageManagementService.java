package com.ift.txmessage.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ift.txmessage.entity.TxMessage;
import com.ift.txmessage.entity.TxMessageContent;
import com.ift.txmessage.mapper.TxMessageContentMapper;
import com.ift.txmessage.mapper.TxMessageMapper;
import com.ift.txmessage.support.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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

    /**
     * 最大重推时间
     */
    private static final LocalDateTime MAX_NEXT_SCHEDULE_TIME =
            LocalDateTime.of(2999, 1, 1, 0, 0, 0);
    /**
     * 退避初始化值
     */
    private static final int DEFAULT_INIT_BACKOFF = 10;
    /**
     * 退避因子
     */
    private static final int DEFAULT_BACKOFF_FACTOR = 2;
    /**
     * 最大重试次数
     */
    private static final int DEFAULT_MAX_RETRY_TIMES = 5;
    /**
     * 每次重推数量
     */
    private static final int SIZE = 100;

    /**
     * 保存消息记录
     *
     * @param txMessage 消息信息
     * @param content   消息内容
     */
    public void saveTransactionalMessageRecord(TxMessage txMessage, String content) {
        txMessage.setMessageStatus(MessageStatus.PENDING.getStatus());
        txMessage.setNextScheduleTime(calculateNextScheduleTime(LocalDateTime.now(), DEFAULT_INIT_BACKOFF,
                DEFAULT_BACKOFF_FACTOR, 0));
        txMessage.setCurrentRetryTimes(0);
        txMessage.setInitBackoff(DEFAULT_INIT_BACKOFF);
        txMessage.setBackoffFactor(DEFAULT_BACKOFF_FACTOR);
        txMessage.setMaxRetryTimes(DEFAULT_MAX_RETRY_TIMES);
        txMessageMapper.insert(txMessage);
        TxMessageContent txMessageContent = new TxMessageContent();
        txMessageContent.setId(UUID.randomUUID().toString().replace("-", ""));
        txMessageContent.setContent(content);
        txMessageContent.setMessageId(txMessage.getMessageId());
        txMessageContent.setCreateUser(txMessage.getCreateUser());
        txMessageContent.setCreateTime(txMessage.getCreateTime());
        txMessageContent.setUpdateUser(txMessage.getUpdateUser());
        txMessageContent.setUpdateTime(txMessage.getUpdateTime());
        txMessageContentMapper.insert(txMessageContent);
    }

    private final MessagePostProcessor messagePostProcessor;
    /**
     * 发送消息
     *
     * @param txMessage 消息信息
     * @param content   消息内容
     */
    public void sendMessageSync(TxMessage txMessage, String content) {
        try {
            rabbitTemplate.convertAndSend(txMessage.getExchangeName(),
                    txMessage.getRoutingKey(), content, messagePostProcessor,
                    new CorrelationData(txMessage.getMessageId()));
        } catch (Exception e) {
            // 标记失败
            markFail(txMessage);
        }
    }

    /**
     * 标记成功
     *
     * @param txMessage 消息信息
     */
    private void markSuccess(TxMessage txMessage) {
        //设置下次重推时间为最大值
        txMessage.setNextScheduleTime(MAX_NEXT_SCHEDULE_TIME);
        if (txMessage.getCurrentRetryTimes() < txMessage.getMaxRetryTimes()) {
            txMessage.setCurrentRetryTimes(txMessage.getCurrentRetryTimes() + 1);
        }
        txMessage.setMessageStatus(MessageStatus.SUCCESS.getStatus());
        txMessage.setUpdateTime(LocalDateTime.now());
        txMessageMapper.updateById(txMessage);
    }

    /**
     * 标记成功
     *
     * @param messageId 消息ID
     */
    public void markSuccess(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            return;
        }
        TxMessage txMessage = txMessageMapper.selectById(messageId);
        if (txMessage == null) {
            return;
        }
        this.markSuccess(txMessage);
    }

    /**
     * 标记失败
     *
     * @param txMessage 消息信息
     */
    private void markFail(TxMessage txMessage) {
        if (txMessage.getCurrentRetryTimes() < txMessage.getMaxRetryTimes()) {
            txMessage.setCurrentRetryTimes(txMessage.getCurrentRetryTimes() + 1);
        }
        // 计算下一次的执行时间
        LocalDateTime nextScheduleTime = calculateNextScheduleTime(
                txMessage.getNextScheduleTime(), txMessage.getInitBackoff(),
                txMessage.getBackoffFactor(), txMessage.getCurrentRetryTimes());
        txMessage.setNextScheduleTime(nextScheduleTime);
        txMessage.setMessageStatus(MessageStatus.FAIL.getStatus());
        txMessage.setUpdateTime(LocalDateTime.now());
        txMessageMapper.updateById(txMessage);
    }

    /**
     * 标记失败
     *
     * @param messageId 消息ID
     */
    public void markFail(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            return;
        }
        TxMessage txMessage = txMessageMapper.selectById(messageId);
        if (txMessage == null) {
            return;
        }
        this.markFail(txMessage);
    }

    /**
     * 计算下一次执行时间
     *
     * @param base          基础时间
     * @param initBackoff   退避基准值
     * @param backoffFactor 退避指数
     * @param round         轮数
     * @return 下一次重推时间
     */
    private LocalDateTime calculateNextScheduleTime(LocalDateTime base, long initBackoff,
                                                    long backoffFactor, long round) {
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
        LambdaQueryWrapper<TxMessage> messageQueryWrapper =
                Wrappers.<TxMessage>lambdaQuery()
                        .between(TxMessage::getNextScheduleTime, min, max)
                        .orderByAsc(TxMessage::getNextScheduleTime);
        Page<TxMessage> page = new Page<>();
        page.setSize(SIZE);
        page.setPages(1);
        Map<String, TxMessage> messages =
                txMessageMapper.selectPage(page, messageQueryWrapper).getRecords()
                        .stream().collect(Collectors.toMap(TxMessage::getMessageId, x -> x));
        if (messages.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<TxMessageContent> messageContentQueryWrapper =
                Wrappers.<TxMessageContent>lambdaQuery()
                        .in(TxMessageContent::getMessageId, messages.keySet());
        txMessageContentMapper.selectList(messageContentQueryWrapper).forEach(item -> {
            TxMessage txMessage = messages.get(item.getMessageId());
            sendMessageSync(txMessage, item.getContent());
        });
    }
}
