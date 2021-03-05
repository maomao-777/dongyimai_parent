/*自定义模块*/
//参数一：模块名称 参数二：引入第三方模块
var app = angular.module('dongyimai', [])
/*$sce服务写成过滤器*/
/*
angularJS的过滤器*/
app.filter('trustHtml',['$sce',function ($sce){
    return function (data){
        return $sce.trustAsHtml(data);
    }
}])
