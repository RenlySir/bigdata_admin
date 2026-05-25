package com.bigdata.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.admin.entity.EtlExecution;
import org.apache.ibatis.annotations.Mapper;

/**
 * ETL Execution Mapper
 */
@Mapper
public interface EtlExecutionMapper extends BaseMapper<EtlExecution> {
}
