package com.polar.cloudimage.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.polar.cloudimage.annotation.AuthCheck;
import com.polar.cloudimage.api.imagesearch.ImageSearchApiFacade;
import com.polar.cloudimage.api.imagesearch.module.ImageSearchResult;
import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.DeleteRequest;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.picture.*;
import com.polar.cloudimage.model.entity.Picture;
import com.polar.cloudimage.model.entity.Space;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.enums.PictureReviewStatusEnum;
import com.polar.cloudimage.model.vo.PictureTagCategory;
import com.polar.cloudimage.model.vo.PictureVO;
import com.polar.cloudimage.service.PictureService;
import com.polar.cloudimage.service.SpaceService;
import com.polar.cloudimage.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/16 16:36
 */
@RestController
@Slf4j
@Api(tags = "图片接口")
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SpaceService spaceService;

    /**
     * 本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)   //初始化空间
            .maximumSize(10_000L)       //最大容量
            .expireAfterWrite(Duration.ofMinutes(5))  //写入5分钟后过期
            .build();

    /**
     * 上传图片 &更新图片
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 图片上传请求
     * @param request              请求
     * @return 图片视图
     */
    @PostMapping("/upload")
    @ApiOperation(value = "上传图片 &更新图片")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile
            , PictureUploadRequest pictureUploadRequest
            , HttpServletRequest request) {

        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);

    }


    /**
     * 通过url上传图片 &更新图片
     *
     * @param pictureUploadRequest 图片上传请求
     * @param request              请求
     * @return 图片视图
     */
    @PostMapping("/upload/url")
    @ApiOperation(value = "URL上传图片 &更新图片")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest
            , HttpServletRequest request) {

        //获取url
        String url = pictureUploadRequest.getFileUrl();
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(url, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);

    }


    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除图片")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息
     *
     * @param pictureUpdateRequest 图片更新请求体
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "更新图片信息")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {

        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片信息 仅管理员可用
     *
     * @param id      图片id
     * @param request 请求
     * @return 图片信息
     */
    @GetMapping("/get")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "根据id获取图片信息")
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }


    /**
     * 根据id获取图片视图 给普通用户使用
     *
     * @param id      图片id
     * @param request 请求
     * @return 图片视图
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据id获取图片视图")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        Long spaceId = picture.getSpaceId();
        //如果是私有图片图片校验是否是本人
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }


    /**
     * 分页获取图片列表 仅管理员可用
     *
     * @param pictureQueryRequest 图片查询请求体
     * @return 图片分页
     */
    @PostMapping("/list/page")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取图片列表")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片视图列表 给普通用户使用
     *
     * @param pictureQueryRequest 图片查询请求体
     * @param request             请求
     * @return 图片视图分页
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取图片视图列表")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            //设置只能查看审核通过的图片
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals((space.getUserId()))) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该空间图片");
            }
        }

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取图片视图列表 给普通用户使用
     * 实现多级缓存
     *
     * @param pictureQueryRequest 图片查询请求体
     * @param request             请求
     * @return 图片视图分页
     */
//    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取图片视图列表")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //设置只能查看审核通过的图片
        //空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) {
            //设置只能查看审核通过的图片
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals((space.getUserId()))) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该空间图片");
            }
        }

        //2. 查询缓存
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(jsonStr.getBytes());
        String cacheKey = String.format("cloudimage:listPictureVOByPage:%s", hashKey);
        //先查询本地缓存
        String localCacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (localCacheValue != null) {
            Page<PictureVO> cachedPage = JSONUtil.toBean(localCacheValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        //本地缓存未命中，查询redis分布式缓存
        String redisCacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (redisCacheValue != null) {
            //存入本地缓存
            LOCAL_CACHE.put(cacheKey, redisCacheValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(redisCacheValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 3.查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        //4. 存入缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);
        //redis缓存
        //设置缓存时间 增加随机时间防止雪崩   5分钟+- random
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 600);
        stringRedisTemplate.opsForValue().set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        // 获取封装类
        return ResultUtils.success(pictureVOPage);

    }

    /**
     * 编辑图片信息
     *
     * @param pictureEditRequest 图片编辑请求体
     * @param request            请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑图片信息")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //编辑图片
        pictureService.editPicture(pictureEditRequest, request);
        return ResultUtils.success(true);
    }


    /**
     * 获取图片标签和分类列表
     *
     * @return 图片标签和分类列表
     */
    @ApiOperation(value = "获取图片标签和分类列表")
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param request              请求
     * @return 是否审核成功
     */
    @PostMapping("/review")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "图片审核")
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传请求
     * @param request                     请求
     * @return 上传成功数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "批量抓取上传图片")
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数错误");
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        Integer successCount = pictureService.uploadPictureByBach(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(successCount);
    }

    /**
     * 以图搜图
     *
     * @param searchPictureByPictureRequest 以图搜图请求
     * @return 图片搜索结果列表
     */
    @PostMapping("/search/picture")
    @ApiOperation(value = "以图搜图")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        //参数校验
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR, "请求参数错误");
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片id错误");
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        List<ImageSearchResult> imageSearchResults = ImageSearchApiFacade.searchImage(picture.getUrl());

        return ResultUtils.success(imageSearchResults);
    }


    /**
     * 通过颜色搜索图片
     *
     * @param searchPictureByColorRequest 颜色搜索请求
     * @param request                     请求
     * @return 图片视图列表
     */
    @PostMapping("/search/color")
    @ApiOperation(value = "通过颜色搜索图片")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR, "请求参数错误");
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);

    }
    /**
     * 批量编辑图片信息
     *
     * @param pictureEditByBatchRequest 图片批量编辑请求
     * @param request                   请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit/batch")
    @ApiOperation(value = "批量编辑图片信息")
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }


}
