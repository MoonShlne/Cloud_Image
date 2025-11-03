package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.polar.cloudimage.model.dto.spaceuser.SpaceUserAddRequest;
import com.polar.cloudimage.model.dto.spaceuser.SpaceUserQueryRequest;
import com.polar.cloudimage.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author polar
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-10-28 15:43:55
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员
     *
     * @param spaceUserAddRequest 空间成员添加请求体
     * @return 新增空间成员 id
     */

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员是否合法
     *
     * @param spaceUser 空间成员
     * @param add       是否为添加操作
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);


    /**
     * 获取空间成员包装类
     *
     * @param spaceUser 空间成员
     * @param request   请求
     * @return 空间成员包装类
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类列表
     *
     * @param spaceUserList 空间成员列表
     * @return 空间成员包装类列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);


    /**
     * 获取空间成员查询包装类
     *
     * @param spaceUserQueryRequest 空间成员查询请求体
     * @return 空间成员查询包装类
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}
