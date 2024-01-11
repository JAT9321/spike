package com.jio.service.service.impl;


import com.jio.service.dao.Goods;
import com.jio.service.dao.OrderRecords;
import com.jio.service.mapper.GoodsMapper;
import com.jio.service.mapper.OrderRecordsMapper;
import com.jio.service.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class GoodsServiceImpl implements GoodsService {


    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private OrderRecordsMapper orderRecordsMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void realDoSeckill(Integer goodsId, Integer userId) {
        Goods goods = goodsMapper.selectByPrimaryKey(Long.valueOf(goodsId));
        int finalStock = goods.getStocks() - 1;
        if (finalStock < 0) {
            // 只是记录日志 让代码停下来   这里的异常用户无法感知
            throw new RuntimeException("库存不足：" + goodsId);
        }
        goods.setStocks(finalStock);
        goods.setUpdateTime(new Date());
        // insert 要么成功 要么报错  update 会出现i<=0的情况
        // update goods set stocks =  1 where id = 1  没有行锁
        int i = goodsMapper.updateByPrimaryKey(goods);
        if (i > 0) {
            // 写订单表
            OrderRecords orderRecords = new OrderRecords();
            orderRecords.setGoodsId(goodsId);
            orderRecords.setUserId(userId);
            orderRecords.setCreateTime(new Date());
            // 时间戳生成订单号
            orderRecords.setOrderSn(String.valueOf(System.currentTimeMillis()));
            orderRecordsMapper.insert(orderRecords);
        }
    }
}
