//控制层
app.controller('goodsController', function ($scope, $controller,$location, goodsService, uploadService,itemCatService,typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        var id =$location.search()['id'];
        if(id==null){
            return ;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;

                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表
                $scope.entity.goodsDesc.itemImages=
                    JSON.parse($scope.entity.goodsDesc.itemImages);
                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                //规格
                	$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                //SKU列表规格列转换
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec =
                        JSON.parse( $scope.entity.itemList[i].spec);
                }

            }
        );
    }
//根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue=function(specName,optionName){
        var items= $scope.entity.goodsDesc.specificationItems;
        var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }

    //保存
    $scope.save = function () {
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction=editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {

                    location.href="goods.html";//跳转到商品列表页
                } else {
                    alert(response.message);
                }
            }
        );
    }
               //定义商品复合类 (商品,商品扩展信息,sku)
    $scope.entity = {'goods': {}, 'goodsDesc': {'itemImages': [], 'specificationItems': []}}
    //保存
    $scope.add = function () {
        //获取商品介绍
        $scope.entity.goodsDesc.introduction = editor.html()
        goodsService.add($scope.entity).success(function (result) {
            if (result.success) {
                alert('保存成功！');
             /*   清空*/
                $scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };
                editor.html('')//清空富文本编辑器
            } else {
                alert(result.message)
            }

        })
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //文件上传
    $scope.item_image_entity={};//表单图像的数据结构
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
        //创建一个json对象来存储图片上传的相关内容（name加url）
            $scope.item_image_entity={};
            if (response.success) {
                $scope.item_image_entity.url=response.message;//后端传递来的url
            }else{
                alert(response.message);
            }
        }).error(function () {
            alert("图片上传发生异常");
        })
    }
    //图片上传的增加和删除行
    $scope.addTableRow=function () {
        $scope.entity.goodsDesc.itemImages.push($scope.item_image_entity);
    }
    $scope.deleteTableRow=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }
//获取文件上传下拉列表
    //查询一级分类列表
    $scope.selectItemCat1List=function () {
        itemCatService.findByParentId("0").success(function (response) {
            $scope.itemCat1List=response;
        });
    }
    //查询二级分类列表$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数。
    //根据一级列表的id是否发生变化
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
        if(newValue){
            //根据选择的值，查询二级分类
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat2List=response;
            })
        }
    });

    //查询三级目录
    $scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
        //判断二级分类有选择具体分类值，在去获取三级分类
        if(newValue){
            //根据选择的值，查询二级分类
            itemCatService.findByParentId(newValue).success(
                function(response){
                    $scope.itemCat3List=response;
                }
            );
        }
    });
    //三级分类选择后  读取模板ID
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        if (newValue){
            itemCatService.findOne(newValue).success(function (response) {
                $scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID
            })
        }
    });
    //模板ID选择后  更新品牌列表 以及获取扩展属性
    $scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
        if(newValue){
            typeTemplateService.findOne(newValue).success(
                function(response){
                    $scope.typeTemplate=response;//获取类型模板
                    $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
                    if($location.search()['id']==null){
                        $scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
                    }


                }
            );
            typeTemplateService.findSpecList(newValue).success(
                function(response){
                    $scope.specList=response;
                }
            );

        }
    });
             //name是规格名称，value规格选项
    $scope.updateSpecAttribute=function($event,name,value){
        //判断选中的规格对象是否在specificationItems集合中存在（根据attributeName）值为name
        var object= $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
        if(object!=null){//对象不存在
            if($event.target.checked ){
                object.attributeValue.push(value);
            }else{
                //取消勾选
                object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
                //如果选项都取消了，将此条记录移除
                if(object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
                }
            }
        }else{
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
        }
    }

    //创建SKU列表
    $scope.createItemList=function(){
        //tb_item（sku）设置的spec规格为空 其它属性为默认
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始
        var items=  $scope.entity.goodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
        }
    }

    //添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
                var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    //状态数组
    $scope.status=['未审核','审核通过','驳回','关闭'];


/*
* 代码解释：因为我们需要根据分类ID得到分类名称，所以我们
* 将返回的分页结果以数组形式再次封装。
*
* */
    $scope.itemCatList=[];//商品分类列表
//加载商品分类列表
    $scope.findItemCatList=function(){
        itemCatService.findAll().success(
            function(response){
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        );
    }
});