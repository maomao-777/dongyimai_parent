app.controller('seckillcontroller', function ($scope, $location, $interval,seckillService) {
    $scope.findList = function () {
        seckillService.findList().success(function (response) {
            $scope.seckillList = response;
        })
    }

    $scope.findOne = function () {
        seckillService.findOneFromRedis($location.search()['id']).success(function (response) {
            $scope.seckillGoods = response;
            //求出距离结束的毫秒数
            $scope.secends = Math.floor((new Date($scope.seckillGoods.endTime).getTime() - new Date().getTime()) / 1000);
            time = $interval(function () {
                if ($scope.secends > 0) {
                    $scope.secends = $scope.secends - 1;
                    $scope.timeString = convertTimeString($scope.secends);

                } else {
                    $interval.cancel(time);
                }
            }, 1000)
        })
    }

    //格式日期显示
    convertTimeString = function (secends) {
        var day = Math.floor(secends / (60 * 60 * 24));//天
        var hours = Math.floor((secends - day * 60 * 60 * 24) / (60 * 60));  //小时
        var min = Math.floor((secends - day * 60 * 60 * 24 - hours * 60 * 60) / 60);  //分
        secends = Math.floor(secends - day * 60 * 60 * 24 - hours * 60 * 60 - min * 60);//秒
        var timestr = "";
        if (day > 0) {
            timestr = day + "天 ";
        }
        return timestr + hours + ":" + min + ":" + secends;
    }
//提交订单
    $scope.submitOrder=function () {
        seckillService.submitOrder($scope.seckillGoods.id).success(function (response) {
          if (response.success){
              //跳转到支付页面
              location.href="pay.html";
          }   else {
              if(response.message=='当前用户未登录'){
                  location.href='login.html';
              }
          }
        })
    }

})