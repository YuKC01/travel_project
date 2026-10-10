package com.travel.service.impl;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.travel.service.QiNiuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class QiNiuServiceImpl implements QiNiuService {

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 1. 生成唯一文件名，避免重名覆盖
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 2. 构造七牛云配置，自动识别机房
            Configuration cfg = new Configuration(Region.autoRegion());
            UploadManager uploadManager = new UploadManager(cfg);

            // 3. 生成上传凭证
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);

            // 4. 流方式上传，不用落本地文件
            InputStream inputStream = file.getInputStream();
            Response response = uploadManager.put(inputStream, fileName, upToken, null, null);

            // 5. 校验上传结果
            if (!response.isOK()) {
                log.error("七牛云上传失败，响应：{}", response.bodyString());
                throw new RuntimeException("图片上传失败");
            }

            // 6. 拼接完整访问URL返回
            return domain + "/" + fileName;

        } catch (Exception e) {
            log.error("七牛云上传异常", e);
            throw new RuntimeException("图片上传异常");
        }
    }
}
