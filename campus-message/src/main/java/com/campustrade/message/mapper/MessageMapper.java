package com.campustrade.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campustrade.message.entity.MessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {
}
