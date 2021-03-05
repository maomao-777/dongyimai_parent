app.controller("itemPageController",function ($scope,$http) {
    /*----------day15静态页面购买商品数量的加减操作方法-----------*/
    $scope.add=function (num) {
        $scope.num+=num;
        if($scope.num<1){
            $scope.num=1;
        }
    }

    //初始化商品静态页面上的规格数据
    $scope.specificationItems={};
    $scope.selectSpecification=function (name,value) {
        $scope.specificationItems[name] =value;
        $scope.searchSku();
    }
    //判断是否是选中的规格选项，以便在前端绑定样式
    $scope.isSpecification=function (name,value) {
        if($scope.specificationItems[name]==value){
            return true;
        }else{
            return false;
        }
    }

   /* //加载sku信息放置第一个sku信息*/
    $scope.loadsku=function () {
        $scope.sku=skuList[0];
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //选中规格将该规格的sku信息显示
    $scope.searchSku=function () {
        //遍历skuList
        for(var i=0;i<skuList.length;i++){
            //比较选中的规格和后端获取的对应sku对象的规格是否一致
            if(mathObject($scope.specificationItems,skuList[i].spec)){
               $scope.sku=skuList[i];
               return;
            }
        }
        //如果规格都不符合
        $scope.sku={'id':0,'title':'---','price':'0','spec':{}};
    }
    mathObject=function (map1,map2) {
        for(var key in map1){
            if (map1[key]!=map2[key]){
                return false;
            }
        }
        for(var key in map2){
            if (map2[key]!=map1[key]){
                return false;
            }
        }
        return true;
    }

    //加入购物车
    $scope.addItemCart = function (){
        //alert('skuId:'+$scope.sku.id);
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
            function (response){
                if(response.success){
                    location.href="http://localhost:9108/cart.html";
                }else{
                    alert(response.message);
                }
            })
    }
});