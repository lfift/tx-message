package com.ift.txmessage.mapper;

import com.ift.txmessage.entity.TxMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 事务消息表 Mapper 接口
 * </p>
 *
 * @author liufei
 * @since 2021-02-01
 */
@Mapper
public interface TxMessageMapper extends BaseMapper<TxMessage> {

}
