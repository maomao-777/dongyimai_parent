app.controller('payController',function ($scope,$location,payService) {
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            $scope.outTradeNo=response.out_trade_no;//订单号
            $scope.totalMoney=(response.total_amount/100).toFixed(2); //交易金额
            var qr = new QRious({
                element:document.getElementById('erweima'),
                size:250,
                level:'H',
                value:response.qrCode
            });
            $scope.queryPayStatus($scope.outTradeNo);
        })
    }
//根据订单编号：查询交易状态
    $scope.queryPayStatus=function () {
        payService.queryPayStatus().success(function (response) {
            if(response.success){
                location.href="paysuccess.html#?money="+$scope.totalMoney;
            }else {
             if (response.message="二维码超时"){
                 //在pay页面显示二维码超时65
                 document.getElementById("timeout").innerHTML="二维码已过期,请重新刷新页面";
             }
                location.href="payfail.html";
            }
        })
    }
    //地址路由，重字符页面跳转到支付成功页面
    $scope.getMoney=function () {
        $location.search()['money'];
    }

})