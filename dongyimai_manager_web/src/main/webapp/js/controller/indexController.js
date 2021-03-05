app.controller("indexController",function ($scope,loginService) {
    $scope.getName = function () {
        loginService.getName().success(function (result) {
            $scope.loginName = result.loginName
        })
    }
})