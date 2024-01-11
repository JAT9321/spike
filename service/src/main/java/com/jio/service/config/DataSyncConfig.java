package com.jio.service.config;


import com.jio.service.dao.Goods;
import com.jio.service.mapper.GoodsMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class DataSyncConfig {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsMapper goodsMapper;


    // 业务场景是搞一个定时任务 每天10点开启
    // 为了 测试方便 项目已启动就执行一次

    /**
     * spring bean的生命周期
     * 在当前对象 实例化完以后
     * 属性注入以后
     * 执行 PostConstruct 注解的方法
     */

    @PostConstruct
    @Scheduled(cron = "0 10 0 0 0 ?")
    public void initData() {
        List<Goods> seckillGoods = goodsMapper.selectSeckillGoods();
        if (CollectionUtils.isEmpty(seckillGoods)) {
            return;
        }
        seckillGoods.forEach(goods -> redisTemplate.opsForValue().set("goods_stock:" + goods.getId(), goods.getStocks().toString()));
    }
}
