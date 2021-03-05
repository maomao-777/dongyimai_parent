app.service("uploadService", function ($http) {
    this.uploadFile = function () {
        //1.获取表单的上传文件的控件（没有双向绑定）
        var formData = new FormData();
        //2.获取上传文件的表单控件的数据'flie'是upload(MultipartFile file)接受参数
        //file.files[0]是表单上传文件按钮上传的一个文件
        formData.append('file', file.files[0]);
        //3.开始上传文件
        return $http({
            /*http解析{}json对象可以有多个参数*/
            'method':'POST',
            'url': '../upload.do',
            'data': formData,
            'headers': {'Content-Type': undefined}, /*anjularjs对于post和get请求默认的Content-Type header 是application/json。通过设置‘Content-Type’: undefined，这样浏览器会帮我们把Content-Type 设置为 multipart/form-data.*/
            'transformRequest': angular.identity /*对数据进行序列化*/
        });

    }
})