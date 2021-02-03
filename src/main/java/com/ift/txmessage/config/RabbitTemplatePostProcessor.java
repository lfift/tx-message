package com.ift.txmessage.config;

import com.ift.txmessage.core.TxMessageManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ相关配置
 *
 * @author liufei
 * @date 2021/2/3 14:30
 */
@Slf4j
@Component
public class RabbitTemplatePostProcessor implements BeanPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //对RabbitTemplate
        if (RabbitTemplate.class.equals(bean.getClass())) {
            /*
             * 消息投递成功与否的监听，可以用来保证消息100%投递到rabbitMQ
             * 需要开启publisher-confirm-type: correlated，旧版本使用publisher-confirms: true
             */
            RabbitTemplate rabbitTemplate = (RabbitTemplate) bean;
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (correlationData == null) {
                    return;
                }
                String correlationDataId = correlationData.getId();
                TxMessageManagementService managementService =
                        applicationContext.getBean(TxMessageManagementService.class);
                if (ack) {
                    //投递成功，修改状态
                    managementService.markSuccess(correlationDataId);
                } else {
                    //NACK投递失败进行重发
                    managementService.markFail(correlationDataId);
                }
            });
            //路由失败回调，需要开启publisher-return: true且template: mandatory: true否则Rabbit将丢弃消息
            rabbitTemplate.setReturnsCallback(returned -> {
                //message, replyCode, replyText, exchange, routingKey
                log.warn("路由失败 message: [{}], replyCode: [{}], replyText: [{}], exchange: [{}], " +
                        "routingKey: [{}]", returned.getMessage(), returned.getReplyCode(),
                        returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
            });
        }
        return bean;
    }
}
