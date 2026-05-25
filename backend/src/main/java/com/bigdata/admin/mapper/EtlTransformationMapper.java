package com.bigdata.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.admin.entity.EtlTransformation;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL Transformation Mapper
 */
@Mapper
public interface EtlTransformationMapper extends BaseMapper<EtlTransformation> {
}
