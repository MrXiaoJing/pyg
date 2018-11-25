app.controller('baseController',function ($scope) {
    $scope.paginationConf={
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function() {
            $scope.reloadList();//重新加载
        }
    }
//刷新
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }
    //从集合中按照key查询对象
    $scope.searchObjectByKey=function (list,key,keyValue) {
        for(var i=0;i<list.length;i++){
            if(list[i][keyValue]==key){
                return list[i];
            }
        }
        return null;
    }
//勾选中的
    $scope.selectIds=[];
    $scope.updateSelection=function ($event,id) {
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1)
        }
    }
})