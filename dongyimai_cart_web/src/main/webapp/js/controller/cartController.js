app.controller('cartController',function ($scope,cartService,addressService) {
   $scope.findCartList=function () {
       cartService.findCartList().success(function (response) {
           $scope.cartList=response;
           $scope.totalValue=cartService.sum($scope.cartList);
       })
   }
   //添加购买数量
    $scope.addGoodsToCartList=function (itemId,num) {
       cartService.addGoodsToCartList(itemId,num).success(function (response) {
           if (response.success){
               //刷新购物车列表
               $scope.findCartList();
           } else{
               alert(response.message);
           }
       })

    }

    //获取用户所有收货地址列表
    $scope.findAddressListByUserId=function () {
        addressService.findAddressListByUserId().success(function (response) {
            $scope.addressList=response;
            //遍历获取默认的地址将其赋给选择地址
            for(var i=0;i<$scope.addressList.length;i++){
             if ($scope.addressList[i].isDefault=='1'){
                 $scope.address=$scope.addressList[i];
                 $scope.addressfirst=$scope.addressList[0];
                 $scope.addressList[0]=$scope.addressList[i];
                 $scope.addressList[i]=$scope.addressfirst;
             }
            }

        })
    }
    
    //要选择收货地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    //给所选的收货地址加选中标志(判断选中的是否是选择的地址)
    $scope.isSelect=function (address) {
        if($scope.address==address){
            return true;
        }else{
            return false;
        }
    }

    //创建订单对象
    $scope.order={'paymentType':'1'}//支付类型，1、在线支付，2、货到付款，默认为1
    //选择支付方式
    $scope.selectPaymentType=function (type) {
        $scope.order.paymentType=type;
    }
  /*  day22*/
    //设置订单
    $scope.submitOrder=function () {//在getOrderInfo.html调用
        //这里需要设置来这getOrderInfo.html组件上的数据到订单属性上
        $scope.order.receiverAreaName=$scope.address.address;//收货地址
        $scope.order.receiverMobile=$scope.address.mobile;//收货电话
        $scope.order.receiver=$scope.address.contact;//收货人
        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success){
                if($scope.order.paymentType=='1'){//根据支付方式跳转不同页面
                 location.href="pay.html";
                }else {
               location.href="paysuccess.html";
                }
                //跳转到支付页面
            } else {
                alert(response.message);
            }
        })
    }
})