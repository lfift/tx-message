package com.ift.txmessage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 事务消息
 *
 * @author liufei
 */
@MapperScan("com.ift.txmessage.mapper")
@SpringBootApplication
public class TxMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(TxMessageApplication.class, args);
    }

}
