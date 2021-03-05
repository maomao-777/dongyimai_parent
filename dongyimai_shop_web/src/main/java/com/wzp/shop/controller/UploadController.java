package com.wzp.shop.controller;

import com.offcn.utils.FastDFSClient;
import com.wzp.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//文件上传
@RestController
public class UploadController {
    //1.通过@Value注解获取application.properties获取文件上传路径的IP地址部分
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;//文件服务器地址

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {

        try {
            //2.获取上传文件的扩展名
            //获取文件名
            String originalFilename = file.getOriginalFilename();
            //截取文件扩展名
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //3.创建一个文件上传的FastDFS客户端(工具类FastDFSClient里面生成了文件上传所需的服务器)
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:conf/fdfs_client.conf");//参数是客户端配置文件,可能存在异常
            //4.调用工具类的执行文件上传的
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);//字节信息，以及扩展名，返回一个文件在数据库存入的地址
            //5.拼接返回的 url 和 ip 地址，拼装成完整的 url ,提供给保存按钮，保存到数据库
            return new Result(true,FILE_SERVER_URL + path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }
    }

}
