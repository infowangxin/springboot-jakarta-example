package com.nutcracker.example.demo.service.auth;

import com.github.pagehelper.PageInfo;
import com.nutcracker.example.demo.entity.ApiResponse;
import com.nutcracker.example.demo.entity.domain.auth.SysPermission;

import java.util.List;

/**
 * 权限服务
 *
 * @author 胡桃夹子
 * @date 2025/01/02 14:44:58
 */
public interface SysPermissionService {

    /**
     * 查找所有系统权限
     *
     * @return {@link List }<{@link SysPermission }>
     */
    List<SysPermission> findAllSysPermission();

    /**
     * 查询用户所能访问的所有菜单
     *
     * @param userId 用户ID
     * @return {@link List }<{@link SysPermission }>
     */
    List<SysPermission> getMenuPermissionByUserId(String userId);

    /**
     * 保存菜单
     *
     * @param sysPermission 菜单
     */
    ApiResponse<Boolean> savePermission(SysPermission sysPermission);

    /**
     * 删除权限
     *
     * @param id 本我
     * @return {@link ApiResponse }<{@link Boolean }>
     */
    ApiResponse<Boolean> deletePermission(String id);

    /**
     * 分页查询资源
     *
     * @param pageNum 当前页码
     * @param permission  {@link SysPermission }
     * @return {@link List }<{@link SysPermission }>
     */
    PageInfo<SysPermission> findSysPermissionByPage(Integer pageNum , SysPermission permission);

}
