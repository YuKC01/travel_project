package com.travel.controller;

import com.travel.common.Result;
import com.travel.service.QiNiuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileUploadController {

    // 注入接口，不注入实现类
    private final QiNiuService qiNiuService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 基础校验
        if (file.isEmpty()) {
            return Result.fail(400, "上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只支持上传图片文件");
        }

        // 2. 调用服务上传
        String imageUrl = qiNiuService.uploadImage(file);
        return Result.success(imageUrl);
    }
}
