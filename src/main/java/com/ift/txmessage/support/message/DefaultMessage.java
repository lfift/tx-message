package com.ift.txmessage.support.message;

import lombok.Builder;

/**
 * 事务消息实现
 *
 * @author liufei
 * @date 2021/2/2 10:22
 */
@Builder
public class DefaultMessage implements Message {

    private final String businessModule;
    private final String businessKey;
    private final String content;
    private final String messageId;
    /**
     * 业务模块
     *
     * @return 业务模块
     */
    @Override
    public String businessModule() {
        return this.businessModule;
    }

    /**
     * 业务键
     *
     * @return 业务键
     */
    @Override
    public String businessKey() {
        return this.businessKey;
    }

    /**
     * 消息内容
     *
     * @return 消息内容
     */
    @Override
    public String content() {
        return this.content;
    }

    /**
     * 消息ID
     *
     * @return 消息ID
     */
    @Override
    public String messageId() {
        return this.messageId;
    }
}
