package com.swapu.controller;

import com.swapu.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 1. 获取原文件名
        String originalFilename = file.getOriginalFilename();
        // 2. 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 3. 生成新文件名 (UUID + 时间 + 后缀)
        String fileName = UUID.randomUUID().toString() + suffix;
        
        // 4. 按日期分类存储
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String datePath = sdf.format(new Date());
        
        File dest = new File(uploadPath + datePath + fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            // 5. 保存文件
            file.transferTo(dest);
            // 6. 返回访问URL (相对路径，由前端拼接或后端配置映射)
            // 假设映射路径为 /files/** -> D:/SwapU/uploads/
            String fileUrl = "/files/" + datePath + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败");
        }
    }
}
