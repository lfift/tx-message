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
public enum MessageStatus {

    /**
     * 待推送
     */
    PENDING(0),
    /**
     * 推送成功
     */
    SUCCESS(1),
    /**
     * 推送失败
     */
    FAIL(2),

    ;

    private final Integer status;
}
