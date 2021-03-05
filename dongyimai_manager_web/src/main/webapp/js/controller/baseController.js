app.controller('baseController',function ($scope) {
    /*调用分页函数*/
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
    }
    /*分页控件设置*/
    $scope.paginationConf = {
        currentPage: 1,//当前页
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页显示条数
        perPageOptions: [10, 20, 30, 40, 50],//每页显示条数下拉选项
        onChange: function () {
            $scope.reloadList()
        }

    }
    /*将选中的集合封装到数组中*/
    $scope.selectIds = []
    $scope.selection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id)
        } else {
            var index = $scope.selectIds.indexOf(id)
            $scope.selectIds.splice(index, 1)
        }
    }

    //提取json数据中某个key的值，返回拼接成字符串
    $scope.jsonToString = function (jsonString,key) {
       if(jsonString!=null){
            var json = JSON.parse(jsonString)//将json字符串转换成json数据
            var value = ""
            for(var i = 0 ; i < json.length ; i++){
                if (i > 0){
                    value += ","
                }

                value += json[i][key]
            }
            return value
        }

    }
})
