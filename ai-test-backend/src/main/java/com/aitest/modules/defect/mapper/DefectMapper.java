package com.aitest.modules.defect.mapper;

import com.aitest.modules.defect.entity.Defect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DefectMapper extends BaseMapper<Defect> {
}
