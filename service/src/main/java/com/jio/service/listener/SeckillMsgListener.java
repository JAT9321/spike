package com.jio.service.listener;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jio.service.mapper.GoodsMapper;
import com.jio.service.service.GoodsService;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.glassfish.jaxb.core.v2.TODO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(topic = "seckillTopic", consumerGroup = "seckill-consumer-group")
public class SeckillMsgListener implements RocketMQListener<MessageExt> {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public void onMessage(MessageExt message) {

        String msg = new String(message.getBody());
        JSONObject jsonObject = JSON.parseObject(msg);
        Integer goodsId = jsonObject.getInteger("goodsId");
        Integer userId = jsonObject.getInteger("userId");
        // 做真实的抢购业务  减库存 写订单表


        // TODO 方法一 ： 直接上锁，这个锁要把整个service.realDoSeckill包起来，因为这个方法realDoSeckill是有事务，要放在事务外面默认事务是可重复读，多个线程会进入事务后读取到相同的值再加锁也会造成数据不正确的现象
        //synchronized (SeckillMsgListener.class) {
        //     goodsService.realDoSeckill(goodsId, userId);
        //}

        // TODO 方法二 : 在数据库层面进行上锁，数据库在执行： 其中的stocks = stocks - 1会自动进行行级别的上锁，保证数据的一致性。
        // update goods set stocks = stocks - 1 ,update_time = now() where id = #{value}

        // TODO 方法三 ： 借用redis实现分布式锁，并且添加上了自旋。
        // redis分布式锁
        while (true) {
            Boolean flag = redisTemplate.opsForValue().setIfAbsent("goods_lock:" + goodsId, "", 20, TimeUnit.SECONDS);
            if (flag) {
                try {
                    goodsService.realDoSeckill(goodsId, userId);
                    return;
                } finally {
                    redisTemplate.delete("goods_lock:" + goodsId);
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
