package com.jio.service.mapper;

import com.jio.service.dao.OrderRecords;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author sky
 * @description 针对表【order_records】的数据库操作Mapper
 * @createDate 2024-01-10 14:53:30
 * @Entity .dao.OrderRecords
 */
@Mapper
public interface OrderRecordsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OrderRecords record);

    int insertSelective(OrderRecords record);

    OrderRecords selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderRecords record);

    int updateByPrimaryKey(OrderRecords record);

}
