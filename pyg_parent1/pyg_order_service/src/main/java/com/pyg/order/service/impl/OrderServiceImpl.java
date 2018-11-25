package com.pyg.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojogroup.Cart;
import com.pyg.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.pojo.TbOrder;
import com.pyg.pojo.TbOrderExample;
import com.pyg.pojo.TbOrderExample.Criteria;
import com.pyg.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//得到购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		List<String> orderIdList = new ArrayList<>();//订单id列表
		double total_money=0.0;
		for (Cart cart : cartList) {
			long orderId = idWorker.nextId();
			orderIdList.add(orderId+"");//添加到订单列表
			System.out.println("sellerId:"+cart.getSellerId());
			//新创建订单对象
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(orderId);//订单id
			tbOrder.setUserId(order.getUserId());//用户名
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setStatus("1");//状态：未付款
			tbOrder.setCreateTime(new Date());//订单创建时间
			tbOrder.setUpdateTime(new Date());//订单修改时间
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//手机号
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType(order.getSourceType());//订单来源
			tbOrder.setSellerId(cart.getSellerId());//商家id
			//循环购物车明细
			double money=0;
			for(TbOrderItem orderItem : cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);//订单id
				orderItem.setSellerId(cart.getSellerId());
				money+=orderItem.getTotalFee().doubleValue();//金额累加
				orderItemMapper.insert(orderItem);
			}
			total_money+=money;//累加到总金额
			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
		}
		if("1".equals(order.getPaymentType())){//微信支付
			TbPayLog payLog = new TbPayLog();
			//主键
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setCreateTime(new Date());//创建时间
			payLog.setTradeState("0");//支付状态
			payLog.setUserId(order.getUserId());//用户id
			payLog.setPayType(order.getPaymentType());//支付类型
			payLog.setTotalFee((long)(total_money*100));//总金额
			//订单号列表，逗号分隔
			String ids = orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
			//订单列表，逗号分隔
			payLog.setOrderList(ids);
			//插入到日志表中
			payLogMapper.insert(payLog);
			//存放到redis缓存中
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
		}
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	//根据用户查询paylong
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}
	//修改订单状态
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//修改（父订单）支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setCreateTime(new Date());//修改时间
		payLog.setTradeState("1");//已付
		payLog.setTransactionId(transaction_id);//交易号（流水）
		payLogMapper.updateByPrimaryKey(payLog);
		//修改（子）订单状态
		String orderList = payLog.getOrderList();
		//将逗号去除，得到订单列表
		String[] orderIds = orderList.split(",");
		//遍历订单列表，得到子订单号
		for (String orderId : orderIds) {
			//根据子订单查询订单，返回订单对象
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			//判断订单是否为空
			if(order!=null){
				//不为null，就将状态改为以支付
				order.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

}