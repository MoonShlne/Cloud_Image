package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.polar.cloudimage.model.dto.space.SpaceAddRequest;
import com.polar.cloudimage.model.dto.space.SpaceQueryRequest;
import com.polar.cloudimage.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author polar
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-10-23 15:05:50
 */
public interface SpaceService extends IService<Space> {



    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求
     * @param loginUser      登录用户
     * @return 新增空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间信息
     *
     * @param space 空间信息
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间视图
     *
     * @param space   空间
     * @param request 请求
     * @return 空间视图
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);


    /**
     * 获取空间视图分页
     *
     * @param spacePage 空间分页
     * @param request   请求
     * @return 空间视图分页
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 获取空间查询包装类
     *
     * @param spaceQueryRequest 空间查询请求体
     * @return 空间查询包装类
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 根据空间等级填充空间信息
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

}
