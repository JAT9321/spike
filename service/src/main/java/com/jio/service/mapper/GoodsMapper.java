package com.jio.service.mapper;


import com.jio.service.dao.Goods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author sky
 * @description 针对表【goods】的数据库操作Mapper
 * @createDate 2024-01-10 14:53:30
 * @Entity .dao.Goods
 */
@Mapper
public interface GoodsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Goods record);

    int insertSelective(Goods record);

    Goods selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Goods record);

    int updateByPrimaryKey(Goods record);

    List<Goods> selectSeckillGoods();

}
