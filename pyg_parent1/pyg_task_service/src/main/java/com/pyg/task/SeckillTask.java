package com.pyg.task;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    //刷新秒杀商品
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("执行了任务调度"+sdf.format(date));
        //查询所有秒杀商品键集合
        List ids = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        //查询正在秒杀的商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//剩余库存大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        criteria.andIdNotIn(ids);//排除缓存中已经有的商品
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //装入缓存
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            System.out.println("将id为"+seckillGoods.getId()+"商品放入缓存");
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
        }
        System.out.println("将新增商品"+seckillGoodsList.size()+"放入缓存");
    }
    //移除秒杀商品
    @Scheduled(cron = "30 * * * * ?")
    public void removeSeckillGoods(){
        System.out.println("移除秒杀商品任务执行");
        //扫描缓存中秒杀商品列表，发现过期的移除
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            if(seckillGoods.getEndTime().getTime()<new Date().getTime()){
                //如果结束日期小于当前日期，则说明已过期
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//向数据保存数据
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());//移除缓存数据
                System.out.println("移除秒杀商品"+seckillGoods.getId());
            }
        }
        System.out.println("移除秒杀商品任务结束");
    }
}
