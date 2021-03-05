app.controller("itemSearchController",function ($scope,$location,itemSearchService) {
    $scope.search=function () {
       //前端页面搜索页码转换整形
        $scope.searchMap.pageNo  = parseInt($scope.searchMap.pageNo);

        itemSearchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
            buildPageLabel();
        })
    }
    //初始化searchMap的数据结构
    $scope.searchMap = {'keywords': '', 'category': '', 'brand': '', 'spec': {},'price':'','pageNo':1,'pageSize':20,'sortField':'','sortValue':''};

    //添加查询条件
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand'||key=='price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
//设置当前页码从第一页查询
        $scope.searchMap.pageNo = 1;
        //执行查询
        $scope.search();
    }

    //移除查询条件
    $scope.removeSearchItem=function(key){
        if(key=="category" ||  key=="brand"||key=='price'){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        //设置当前页码从第一页查询
        $scope.searchMap.pageNo = 1;
        $scope.search();//执行搜索
    }
/*-----------------day14----------------*/
    //构建分页标签(totalPages为总页数)
    buildPageLabel=function () {
        $scope.pageLabel = [];   //初始化数组
        var maxPage = $scope.resultMap.totalPages;
        var firstPage = 1;    //数组的起始页码
        var lastPage = maxPage;//数组的结束页码
        //返回给前端是否在页码前有....
        $scope.firstDot = true;//前面有省略号
        $scope.lastDot = true;//后面有省略号

      //分情况求first和last
   //动态页码
   if(maxPage>5){
       if ($scope.searchMap.pageNo<=3){
           lastPage=5;
           $scope.firstDot = false;
       } else if($scope.searchMap.pageNo>=lastPage-2){
           firstPage=maxPage-4;
           $scope.lastDot = false;
       }else {
           firstPage=$scope.searchMap.pageNo-2;
           lastPage=$scope.searchMap.pageNo+2;
       }
   }else {
       $scope.firstDot = false;
       $scope.lastDot = false;
   }
   //将动态页码页码存入数组
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //提交页码执行查询
    $scope.queryByPage = function (pageNo) {
        //页码格式验证
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        //执行查询
        $scope.search();
    }

    //判断当前页是否是第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }
    //判断当前页是否是最后一页
    $scope.resultMap = {'totalPages': 10};//前端页面调用方法，刚开始totalPages没有值，这里给一个默认值，避免报错
    $scope.isLastPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //判断是否是当前页
    $scope.isPage = function (pageNo) {
        if ($scope.searchMap.pageNo == pageNo) {
            return true;
        } else {
            return false;
        }
    }

    //条件排序
    $scope.sortSearch=function (sortField,sortValue) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sortValue=sortValue;
        //执行查询
        $scope.search();
    }
    //判断关键字是否是品牌信息
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
                return true;
            }
        }
        return false;
    }
   /* day14*/
    //地址路由
    //加载查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();
    }

})

