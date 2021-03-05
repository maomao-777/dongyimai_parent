app.service('seckillService', function ($http) {
    this.findList = function () {
        return $http.get('../seckillGoods/findList.do');
    }
    //商品详情页
    this.findOneFromRedis = function (id) {
        return $http.get('../seckillGoods/findOneFromRedis.do?id=' + id);
    }
    //提交订单
    this.submitOrder=function (seckillId) {
        return $http.get('../seckillOrder/submitOrder.do?seckillId='+seckillId);
    }

})