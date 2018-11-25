package com.pyg.shop.controller;

import com.pyg.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class uploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            String uploadFile = client.uploadFile(file.getBytes(), file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1), null);
            return new Result(true,FILE_SERVER_URL+uploadFile);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"操作失败");
        }
    }
}
