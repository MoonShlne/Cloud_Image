package com.polar.cloudimage.controller;

import com.polar.cloudimage.annotation.AuthCheck;
import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/15 15:39
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件接口")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUpload(@RequestPart("file") MultipartFile multipartFile) {
        //文件目录
        String filename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", filename);

        File file = null;

        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败 path={}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.info("文件删除失败 file={}", file.getAbsolutePath());
                }
            }

        }


    }

    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {

        COSObjectInputStream objectContent = null;
        try {
            //获取流文件
            COSObject object = cosManager.getObject(filePath);
            objectContent = object.getObjectContent();
            //设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + filePath);
            response.setContentType("application/octet-stream;charset=UTF-8");
            //写入相应
            byte[] byteArray = IOUtils.toByteArray(objectContent);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(byteArray);
            outputStream.flush();
        } catch (IOException e) {
            log.info("文件下载失败 path={}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        } finally {
            if (objectContent != null) {
                objectContent.close();
            }
        }


    }

}
