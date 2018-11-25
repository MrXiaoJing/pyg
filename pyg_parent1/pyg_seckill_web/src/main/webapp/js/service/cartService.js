//购物车服务层
app.service('cartService',function($http){
    //购物车列表
    this.findCartList=function(){
       return  $http.get('cart/findCartList.do');
    }
    //添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }
    //求合计数
    this.sum=function(cartList){
        var totalValue={totalNum:0,totalMoney:0.00};//合计实体
        for(var i=0;i<cartList.length;i++){
            var cart = cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem = cart.orderItemList[j];//购物车明细
                totalValue.totalNum+=orderItem.num;
                totalValue.totalMoney+=orderItem.totalFee;
            }
        }
        return totalValue;
    }
    //获取地址列表
    this.findListByUserId=function(){
       return $http.get('address/findListByUserId.do');
    }
    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }
    //增加
    this.add=function(address){
        return  $http.post('address/add.do',address);
    }
    //修改
    this.update=function(address){
        return  $http.post('address/update.do',address );
    }
    //删除
    this.dele=function(ids){
        return $http.get('address/delete.do?ids='+ids);
    }
})