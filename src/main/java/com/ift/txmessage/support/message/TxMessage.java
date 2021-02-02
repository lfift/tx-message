package com.ift.txmessage.support.message;

/**
 * 消息接口
 *
 * @author liufei
 * @date 2021/2/2 10:20
 */
public interface TxMessage {

    /**
     * 业务模块
     *
     * @return 业务模块
     */
    String businessModule();

    /**
     * 业务键
     *
     * @return 业务键
     */
    String businessKey();

    /**
     * 消息内容
     *
     * @return 消息内容
     */
    String content();
}
