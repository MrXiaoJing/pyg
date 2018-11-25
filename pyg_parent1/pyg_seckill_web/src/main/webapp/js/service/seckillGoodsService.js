//服务层
app.service('seckillGoodsService',function($http){
	//查询实体
	this.findOne=function(id){
		return $http.get('../seckillGoods/findOneFromRedis.do?id='+id);
	}
	//当前秒杀商品
    this.findList=function(){
        return $http.get('../seckillGoods/findList.do');
    }
    this.submitOrder=function(id){
	    return $http.get('../seckillOrder/submitOrder.do?seckillId='+id);
    }
});
