package com.ift.txmessage.task;

import com.ift.txmessage.core.TxMessageManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

/**
 * 任务调度配置
 *
 * @author liufei
 * @date 2021/2/2 17:01
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleJobTaskConfig {

    private static final String REDIS_LOCK_KEY = "task:tx:message";

    private final TxMessageManagementService txMessageManagementService;
    private final RedissonClient redissonClient;

//    @Scheduled(fixedDelay = 6000)
    public void scheduleTxMessage() throws InterruptedException {
        RLock lock = redissonClient.getLock(REDIS_LOCK_KEY);
        //等待时间5秒,预期300秒执行完毕,这两个值需要按照实际场景定制
        boolean tryLock = lock.tryLock(5, 300, TimeUnit.SECONDS);
        if (tryLock) {
            try {
                long start = System.currentTimeMillis();
                log.info("开始执行事务消息推送补偿定时任务...");
                txMessageManagementService.processPendingCompensationRecords();
                long end = System.currentTimeMillis();
                long delta = end - start;
                // 以防锁过早释放
                if (delta < 5000) {
                    Thread.sleep(5000 - delta);
                }
                log.info("执行事务消息推送补偿定时任务完毕,耗时:{} ms...", end - start);
            } finally {
                lock.unlock();
            }
        }
    }
}
