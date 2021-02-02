package com.ift.txmessage.support.binding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Queue类型
 *
 * @author liufei
 * @date 2021/2/2 10:08
 */
@Getter
@RequiredArgsConstructor
public enum ExchangeType {

    FANOUT("fanout"),

    DIRECT("direct"),

    TOPIC("topic"),

    DEFAULT("");

    private final String type;
}
