//定义控制器
app.controller('brandController',function ($scope,brandService,$controller) {
    $controller('baseController',{$scope:$scope})
    //查询所有
    $scope.findAll=function () {
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            }
        );
    }
    //分页
    $scope.findPage=function (page,rows) {
        brandService.findPage(page,rows).success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }
//添加

    $scope.save=function () {
        var methodObject;
        if($scope.entity.id!=null){
            methodObject=brandService.update($scope.entity);
        }else{
            methodObject=brandService.add($scope.entity);
        }
        methodObject.success(
            function (response) {
                if(response.success){
                    $scope.reloadList();//成功刷新页面
                }else{
                    alert(response.message);//失败
                }
            }
        );
    }

//回显
    $scope.findOne=function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    }


//删除
    $scope.dele=function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();//成功刷新页面
                }
                $scope.selectIds=[];
            }
        )
    }
//按条件查询
    $scope.searchEntity={};
    $scope.search=function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        );
    }
})
