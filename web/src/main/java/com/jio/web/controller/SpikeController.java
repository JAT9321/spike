package com.jio.web.controller;


import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class SpikeController {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    AtomicInteger ai = new AtomicInteger(10001);

    /**
     * 1.一个用户针对一种商品只能抢购一次
     * 2.做库存的预扣减  拦截掉大量无效请求
     * 3.放入mq 异步化处理订单
     *
     * @return
     */
    @GetMapping("doSeckill")
    public String doSeckill(Integer goodsId) {
        //这个用户id，其实是当用户登录后，会在springsecurity或者shiro里存放的有，从他们的context环境中获取。
        int userId = ai.incrementAndGet();

        // unique key 唯一标记 去重
        String uk = userId + "-" + goodsId;

        // set nx  set if not exist
        Boolean flag = redisTemplate.opsForValue().setIfAbsent("seckillUk:" + uk, "");
        if (Boolean.FALSE.equals(flag)) {
            return "您参与过该商品的抢购，请参与其他商品抢购";
        }

        // 假设库存已经同步了  key:goods_stock:1  val:10
        Long count = redisTemplate.opsForValue().decrement("goods_stock:" + goodsId);
        // getkey  java  setkey    先查再写 再更新 有并发安全问题
        if (count < 0) {
            return "该商品已经被抢完，请下次早点来哦O(∩_∩)O";
        }

        //放入mq
        Map<String, Integer> map = new HashMap<String, Integer>(4);
        map.put("goodsId", goodsId);
        map.put("userId", userId);
        rocketMQTemplate.asyncSend(
                "seckillTopic",
                JSON.toJSONString(map),
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.println("发送成功" + sendResult.getSendStatus());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        System.out.println("发送失败" + throwable);
                    }
                }
        );
        return "拼命抢购中,请稍后去订单中心查看";
    }

}
