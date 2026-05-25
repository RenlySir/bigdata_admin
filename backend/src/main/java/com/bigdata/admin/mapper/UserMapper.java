package com.bigdata.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.admin.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
