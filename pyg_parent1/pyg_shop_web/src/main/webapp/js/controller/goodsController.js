//控制层
app.controller('goodsController' ,function($scope,$controller,$location,typeTemplateService,itemCatService,uploadServcie,goodsService){

    $controller('baseController',{$scope:$scope});//继承
    $scope.status=['未审核','已审核','审核未通过','关闭']
    //商品分类列表
    $scope.itemCatList=[];
    //加载商品分类列表
    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        )
    }

    //读取列表数据绑定到表单中
    $scope.findAll=function(){
        goodsService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }
    $scope.uploadFile=function () {
        uploadServcie.uploadFile().success(
            function (response) {
                if(response.success){
                    //重新查询
                    $scope.entity_image.url=response.message;
                    //文件清空
                    document.getElementById("file").value="";
                }else{
                    alert(response.message);
                }
            }
        )
    }
//查找一级分类
    $scope.selectItemCat1List=function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List=response;
            }
        )
    }
    //查找二级分类
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List=response;
            }
        )
    })
    //查找三级分类
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List=response;
            }
        )
    })
    //查找模板
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId=response.typeId;
            }
        )
    })
    //获取品牌信息
    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate=response;
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
            }
        );
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList=response;
            }
        )
    });
//定义商品实体类
    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};
    //根据规格名称和选项名称返回是否被勾选
    $scope.isChecked=function (sname,pname) {
        var items =$scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items,sname,'attributeName');
        if (object!=null){
            if(object.attributeValue.indexOf(pname)>=0){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    $scope.updateSpecAttribute=function ($event,name,value) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,name,'attributeName');
        if(object!=null){
            if($event.target.checked){
                object.attributeValue.push(value);
            }else{
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                if(object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
                }
            }
        }else{
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
        }
    }
//创建sku列表
    $scope.createItemList=function () {
        $scope.entity.itemList=[{spec:{},price:0,num:999,status:'1',isDefault:'0'}]; //初始化
        var items = $scope.entity.goodsDesc.specificationItems;
        for(var i=0;i<items.length;i++){
            $scope.entity.itemList =addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
        }
    }
//添加列值
    addColumn=function (list,name,values) {
        var newList=[];
        for(var i=0;i<list.length;i++){
            var oldRow=list[i];
            for(var j=0;j<values.length;j++){
                var newRow=JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[name] = values[j];
                newList.push(newRow);
            }
        }
        return newList;
    }
    //分页
    $scope.findPage=function(page,rows){
        goodsService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne=function() {
        var id = $location.search()["id"];//从url上获取参数值
        alert(id)
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction)
                //显示图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //读取规格属性
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            })
    };

    //添加图
    $scope.add_image = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.entity_image);
    }
    //移除图
    $scope.remove_image = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }
    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        $scope.entity.goodsDesc.introduction = editor.html();
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    location.href = "goods.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows){
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
});
