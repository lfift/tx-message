package com.ift.txmessage.support.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息状态
 *
 * @author liufei
 * @date 2021/2/2 10:15
 */
@Getter
@RequiredArgsConstructor
public enum TxMessageStatus {
    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 待处理
     */
    PENDING(0),

    /**
     * 处理失败
     */
    FAIL(2),

    ;

    private final Integer status;
}
