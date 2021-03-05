app.controller("indexController",function ($scope,sellerService) {
    $scope.getName = function () {
        sellerService.getName().success(function (result) {
            $scope.loginName = result.loginName
        })
    }
})