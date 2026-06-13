package com.aitest.modules.llm.mapper;

import com.aitest.modules.llm.entity.LlmMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LlmMessageMapper extends BaseMapper<LlmMessage> {
}
