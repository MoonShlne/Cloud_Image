package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.polar.cloudimage.model.dto.picture.*;
import com.polar.cloudimage.model.entity.Picture;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.PictureVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author polar
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-10-15 16:51:05
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource          源文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片视图
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**
     * 获取图片查询包装类
     *
     * @param pictureQueryRequest 图片查询请求体
     * @return 图片查询包装类
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片视图
     *
     * @param picture 图片
     * @param request 请求
     * @return 图片视图
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片视图分页
     *
     * @param picturePage 图片分页
     * @param request     请求
     * @return 图片视图分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充图片审核参数
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传请求
     * @param loginUser                   登录用户
     * @return 上传成功数量
     */
    Integer uploadPictureByBach(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 异步删除图片文件
     *
     * @param oldPicture 旧图片信息
     */
    @Async
    void clearPictureFile(Picture oldPicture);

    /**
     * 校验图片操作权限
     *
     * @param loginUser 登录用户
     * @param picture   图片
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     *
     * @param pictureId 图片id
     * @param loginUser 登录用户
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片信息
     *
     * @param pictureEditRequest 图片编辑请求体
     * @param request            请求
     */
    void editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId  空间id
     * @param picColor 颜色值
     * @param loginUser 登录用户
     * @return 图片列表
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);


    /**
     * 批量编辑图片信息
     *
     * @param pictureEditByBatchRequest 图片批量编辑请求体
     * @param loginUser                 登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);


    /**
     * 创建图片扩图任务
     *
     * @param createPictureOutPaintingTaskRequest 创建图片扩图任务请求体
     * @param loginUser                           登录用户
     * @return
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

}
