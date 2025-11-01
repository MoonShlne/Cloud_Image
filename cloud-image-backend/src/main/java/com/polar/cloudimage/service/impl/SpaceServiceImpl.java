package com.polar.cloudimage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.space.SpaceAddRequest;
import com.polar.cloudimage.model.dto.space.SpaceQueryRequest;
import com.polar.cloudimage.model.entity.Space;
import com.polar.cloudimage.model.entity.SpaceUser;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.enums.SpaceLevelEnum;
import com.polar.cloudimage.model.enums.SpaceRoleEnum;
import com.polar.cloudimage.model.enums.SpaceTypeEnum;
import com.polar.cloudimage.model.vo.SpaceVO;
import com.polar.cloudimage.model.vo.UserVO;
import com.polar.cloudimage.service.SpaceService;
import com.polar.cloudimage.mapper.SpaceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author polar
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-10-23 15:05:50
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SpaceUserServiceImpl spaceUserServiceImpl;

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求
     * @param loginUser       登录用户
     * @return 新增空间id
     */
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        // 默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }

        // 填充数据
        this.fillSpaceBySpaceLevel(space);
        // 数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        // 权限校验
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        // 针对用户进行加锁  用户只能创建一个私有空间和一个团队空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, space.getSpaceType())        // 私有或团队
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能创建一个");
                // 写入数据库
                ThrowUtils.throwIf(!this.save(space), ErrorCode.OPERATION_ERROR);
                //创建成功后，如果创建团队空间，把自己加入空间成员
                if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(space.getUserId());
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    //存入数据库
                    ThrowUtils.throwIf(!spaceUserServiceImpl.save(spaceUser), ErrorCode.OPERATION_ERROR, "添加空间成员失败");
                }

                // 返回新写入的数据 id
                return space.getId();
            });
            // 返回结果是包装类，可以做一些处理
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    /**
     * 校验空间信息
     *
     * @param space 空间信息
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(ObjUtil.isNull(space), ErrorCode.PARAMS_ERROR);
        //从对象取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        if (add) {
            //创建时，参数不能为空
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(ObjUtil.isNull(spaceLevel), ErrorCode.PARAMS_ERROR, "空间等级不能为空");
            ThrowUtils.throwIf(ObjUtil.isNull(spaceType), ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        }
        //修改数据校验，名称不能长度大于15
        ThrowUtils.throwIf(!StrUtil.isNotBlank(spaceName) && spaceName.length() > 15, ErrorCode.PARAMS_ERROR, "空间名称过长");
        //校验空间级别，如果不是枚举中的值，则报错
        ThrowUtils.throwIf(!ObjUtil.isNotNull(spaceLevel) && ObjUtil.isNull(enumByValue), ErrorCode.PARAMS_ERROR, "空间等级不合法");
        //修改数据时，校验空间类型
        ThrowUtils.throwIf(!ObjUtil.isNotNull(spaceType) && ObjUtil.isNull(spaceTypeEnum), ErrorCode.PARAMS_ERROR, "空间类型不合法");
    }

    /**
     * 获取空间视图
     *
     * @param space   空间
     * @param request 请求
     * @return 空间视图
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVo(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    /**
     * 获取空间视图分页
     *
     * @param spacePage 空间分页
     * @param request   请求
     * @return 空间视图分页
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVo(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 获取空间查询包装类
     *
     * @param spaceQueryRequest 空间查询请求体
     * @return 空间查询包装类
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 获取请求参数
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();


        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),
                sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 根据空间等级填充空间信息
     *
     * @param space 空间
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        //获取空间级别枚举
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        //管理员可以指定空间大小和图片数量
        if (spaceLevelEnum != null) {
            long maxCount = spaceLevelEnum.getMaxCount();
            long maxSize = spaceLevelEnum.getMaxSize();
            //如果管理员未指定，则使用默认值
            if (space.getMaxCount() == null || space.getMaxCount() <= 0) {
                space.setMaxCount(maxCount);
            }
            if (space.getMaxSize() == null || space.getMaxSize() <= 0) {
                space.setMaxSize(maxSize);
            }
        }
    }

    /**
     * 检查空间访问权限
     *
     * @param loginUser 登录用户
     * @param space     空间
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        //仅管理员或本人可删除空间
        ThrowUtils.throwIf((!userService.isAdmin(loginUser) && !loginUser.getId().equals(space.getUserId())), ErrorCode.NO_AUTH_ERROR);
    }
}




