package com.atguigu.gmall.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zdy
 * @create 2019-08-19 20:59
 */
@CrossOrigin
@Controller
public class FileUploadController {

    @Value("${fileServer.url}")
    private String fileUrl;

    @ResponseBody
    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    public String fileUpload(MultipartFile file) throws IOException, MyException {
        String imgUrl=fileUrl;
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(configFile);
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer=trackerClient.getConnection();
        StorageClient storageClient=new StorageClient(trackerServer,null);
        String orginalFilename=file.getOriginalFilename();
        String extName = StringUtils.substringAfter(orginalFilename, ".");
        //System.out.println("extName="+extName);
        String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            imgUrl+="/"+s;
        }

        return imgUrl;
    }

}
