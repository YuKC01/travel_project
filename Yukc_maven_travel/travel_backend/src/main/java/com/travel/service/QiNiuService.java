package com.travel.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 七牛云对象存储服务接口
 */
public interface QiNiuService {

    /**
     * 上传图片到七牛云
     * @param file 前端上传的图片文件
     * @return 完整的图片访问URL
     */
    String uploadImage(MultipartFile file);
}
