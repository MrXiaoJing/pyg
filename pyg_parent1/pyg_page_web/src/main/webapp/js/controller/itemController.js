//商品详情页（控制层）
app.controller('itemController',function($scope,$http){
	$scope.num=1;
	//数量操作
	$scope.changNum=function(num){
		$scope.num=$scope.num+num;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	$scope.specificationItems={}; //记录用户选择的规则
	//用户选择规则
	$scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;
		$scope.searchSku(); //读取sku
	}
	//判断用户某个规格是否被选中
	$scope.isSelected=function(key,value){
		if($scope.specificationItems[key]==value){
			return true;
		}else{
			return false;
		}
	}
	//加载默认sku
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		 $scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	//匹配俩个对象
	$scope.matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	$scope.searchSku=function(){
		for(var i=0;i<skuList.length;i++){
			if($scope.matchObject(skuList[i].spec,$scope.specificationItems)){
				$scope.sku=skuList[i];
				return ;
			}
		}
		$scope.sku={id:0,title:'----------------',price:0}//没有匹配数据
	}
	//添加商品加入购物车
	$scope.addToCart=function(){
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(
			function(response){
				if(response.success){
					location.href="http://localhost:9107/cart.html";//跳转到购物车页面
				}else {
					alert(response.message);
				}
			}
		);
		// alert('skuid'+$scope.sku.id+','+'num'+$scope.num)
	}
});