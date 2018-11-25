//购物车控制层
app.controller('cartController',function($scope,cartService){
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                $scope.totalValue=cartService.sum($scope.cartList);//求合计数
            }
        );
    }
    //添加商品到购物车
    $scope.addGoodsToCartList=function(itemId,num){
        cartService.addGoodsToCartList(itemId,num).success(
            function(response){
                if(response.success){
                    $scope.findCartList();//刷新页面
                }else{
                    alert(response.message);//弹出错误提示
                }
            }
        );
    }
    //获取根据列表
    $scope.findListByUserId=function(){
        cartService.findListByUserId().success(
            function(response){
                $scope.addressList=response;
                //设置默认地址
                for(var i=0;i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1' ){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }
    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }
    //判断是否是在当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    //保存订单
    $scope.submitOrder=function(){
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function(response){
                if(response.success){
                    //页面跳转
                    if($scope.order.paymentType=='1'){
                        //如果是微信支付就跳转到页面
                        location.href="pay.html";
                    }else{
                        //如果是货到付款
                        location.href="paysuccess.html";
                    }
                }else{
                    alert(response.message)//失败就弹出提示
                }
            }
        );
    }





    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.address.id!=null){//如果有ID
            serviceObject=cartService.update( $scope.address ); //修改
        }else{
            serviceObject=cartService.add( $scope.address);//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.findListByUserId();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }
    //批量删除
    $scope.dele=function(){
        //获取选中的复选框
        cartService.dele( $scope.address.id ).success(
            function(response){
                if(response.success){
                    $scope.findListByUserId();//刷新列表
                }
            }
        );
    }
        //选择支付方式
    $scope.order={paymentType:'1'};
    $scope.selectPayType=function(type){
        $scope.order.paymentType=type;
    }
})