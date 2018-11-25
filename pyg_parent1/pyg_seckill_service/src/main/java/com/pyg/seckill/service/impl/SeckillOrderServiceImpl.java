package com.pyg.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.pojo.TbSeckillOrderExample;
import com.pyg.pojo.TbSeckillOrderExample.Criteria;
import com.pyg.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Override
	public void submitOrder(Long seckillId, String userId) {
		//从缓存中查询秒杀商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
	if(seckillGoods==null){
		System.out.println("商品不存在");
	}
	if(seckillGoods.getStockCount()<=0){
		System.out.println("商品已抢购一空");
	}
	//扣除redis库存(先获取到当前库存-1再将新的库存设置到seckillGooods中)
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
	//放入到缓存中
		redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
		if(seckillGoods.getStockCount()==0){//如果已经抢光
			//更新数据库
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			//清除redis缓存
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}
		//保存redis订单
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setCreateTime(new Date());//创建时间
		seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
		seckillOrder.setSellerId(seckillGoods.getSellerId());//商家id
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setUserId(userId);//设置用户id
		seckillOrder.setStatus("1");//状态
		redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);

	}
	//从redis中获取秒杀信息
	@Override
	public TbSeckillOrder searchOrderFormRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void searchOrderFromRedisToDb(String userId, String transaction_id) {
		//1.查询秒杀订单（根据当前登录人）
		TbSeckillOrder seckillOrder = searchOrderFormRedisByUserId(userId);
		//2.修改状态，支付时间，支付微信流水
		seckillOrder.setStatus("2");
		seckillOrder.setPayTime(new Date());
		seckillOrder.setTransactionId(transaction_id);
		//3.将数据保存到数据库中
		seckillOrderMapper.insert(seckillOrder);
		//4.清除redis中的秒杀订单信息
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//根据用户id从redis中获取seckillOrder对象
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		//判断秒杀订单id和orderid
		if(seckillOrder!=null&&seckillOrder.getId().longValue()==orderId.longValue()){
			//删除redis秒杀订单数据
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//恢复库存
			//1.先获取seckillGoods对象
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
			//2.设置库存+1
			if(seckillGoods!=null) {
				seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
				//3.再将新的seckillGoods放入redis中
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods); //存入缓存
			}
		}
	}

}
