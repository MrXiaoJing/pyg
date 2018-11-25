app.controller('searchController',function($scope,searchService,$location,$controller){
    //搜索
     $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':10,'sortField':'','sort':''}; //封装查询条件的map
    $scope.search =function() {
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response; //搜索返回结果
                $scope.buildPageLabel();
            }
        )
    }
    //添加搜索选项
    $scope.addSearchItem=function(name,value){
        if(name=='category'||name=='brand'||name=='price'){//如果点击的是分类或是品牌
            $scope.searchMap[name]=value;
        }else{
            $scope.searchMap.spec[name]=value;
        }
        $scope.search();//执行搜索
    }
    //移除
    $scope.removeSearchItem=function(name){
        if(name=="category"||name=="brand"||name=='price'){//如果是分类或品牌
            $scope.searchMap[name]="";
        }else{
            delete $scope.searchMap.spec[name]; //移除此属性
        }
        $scope.search();//执行搜索
    }
    //构建分页栏
    $scope.buildPageLabel=function(){
        $scope.pageLabel=[];
        var maxPageNo=$scope.resultMap.totalPages;//得到最后页码
        var firstPage=1;//开始页码
        var lastPage=maxPageNo;//截止页码
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后面有点
        if($scope.resultMap.totalPages>5){ //如果总页数大于5页，显示部分页码
            if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
                lastPage=5; //前5页
                $scope.firstDot=false;
            }else if($scope.searchMap.pageNo>=lastPage-2){ //如果当前页大于等于最大页码减2
                firstPage=maxPageNo-4; //后5页
                firstPage=$scope.resultMap.totalPages-4;
                $scope.lastDot=false;
            }else{//显示当前为中心的5页（前二后二）
                firstPage=$scope.searchMap.pageNo-2;  //当前页减2
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后面无点
        }
        //循环产生页码标签
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
    //根据页码查询
    $scope.queryByPage=function(pageNo){
         pageNo =parseInt(pageNo);
        //页码验证
        if(pageNo<1||pageNo>$scope.resultMap.totalPages){
            return ;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
    $scope.isToPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }
    //设置排序规则
    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }
    //判断关键字是不是品牌
    $scope.keywordsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){ //如果包含
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }
    //查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }
})