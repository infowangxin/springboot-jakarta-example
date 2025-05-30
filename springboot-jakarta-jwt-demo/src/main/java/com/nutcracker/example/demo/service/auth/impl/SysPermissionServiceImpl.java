package com.nutcracker.example.demo.service.auth.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nutcracker.common.util.JSON;
import com.nutcracker.common.wrapper.RespWrapper;
import com.nutcracker.example.demo.constant.CacheableKey;
import com.nutcracker.example.demo.convert.auth.SysPermissionConvert;
import com.nutcracker.example.demo.entity.dataobject.auth.SysPermissionDo;
import com.nutcracker.example.demo.entity.dataobject.auth.SysRolePermissionDo;
import com.nutcracker.example.demo.entity.domain.auth.SysPermission;
import com.nutcracker.example.demo.mapper.auth.SysPermissionMapper;
import com.nutcracker.example.demo.mapper.auth.SysRolePermissionMapper;
import com.nutcracker.example.demo.service.auth.SysPermissionService;
import com.nutcracker.example.demo.web.Identify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 权限服务
 *
 * @author 胡桃夹子
 * @date 2025/01/02 15:15:00
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<SysPermission> findSysPermission() {
        List<SysPermissionDo> permissionDoList = sysPermissionMapper.findAll();
        log.info("findAllSysPermission: \n{}", JSON.toJSONString(permissionDoList));
        if (CollUtil.isEmpty(permissionDoList)) {
            return Collections.emptyList();
        }
        List<SysPermission> list = SysPermissionConvert.INSTANCE.toDomain(permissionDoList);
        return getPermissionTree(list);
    }

    @Override
    @Cacheable(cacheNames = CacheableKey.ROLE_PERMISSION, key = "#roleId", condition = "#roleId != null", unless = "#result == null")
    public List<SysPermission> getRolePermissionByRoleId(String roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("roleId cannot be null");
        }
        log.info("getRolePermissionByRoleId roleId={}", roleId);
        List<SysPermissionDo> permissionDoList = sysPermissionMapper.getSysPermissionByRoleId(roleId);
        log.info("getRolePermissionByRoleId, roleId={},permissionDoList.size={}", roleId, CollUtil.size(permissionDoList));
        if (CollUtil.isEmpty(permissionDoList)) {
            return Collections.emptyList();
        }
        List<SysPermission> list = SysPermissionConvert.INSTANCE.toDomain(permissionDoList);
        List<SysPermission> result = getPermissionTree(list);
        log.debug("getRolePermissionByRoleId roleId={},result.size={}", roleId, CollUtil.size(result));
        return result;
    }

    @Override
    public SysPermission getPermission(String id) {
        log.info("getPermission id={}", id);
        SysPermissionDo permissionDo = sysPermissionMapper.selectById(id);
        SysPermission permission = SysPermissionConvert.INSTANCE.toDomain(permissionDo);
        log.info("getPermission id={},permission={}", id, JSON.toJSONString(permission));
        return permission;
    }

    private List<SysPermission> getPermissionTree(List<SysPermission> permissionList) {
        // 如果查询结果为空，直接返回空列表
        if (CollectionUtil.isEmpty(permissionList)) {
            return Collections.emptyList();
        }

        // 将 DO 对象转换为 VO 对象，并按菜单级别分组
        Map<Integer, List<SysPermission>> menuLevelMap = groupPermissionsByLevel(permissionList);
        // 获取所有层级
        List<Integer> levels = new ArrayList<>(menuLevelMap.keySet());
        // 从大到小排序
        levels.sort(Comparator.reverseOrder());

        List<SysPermission> result = null;
        for (Integer level : levels) {
            List<SysPermission> menuList = menuLevelMap.get(level);
            int parentLevel = Math.max(0, level - 1);
            // 获取上一级菜单
            List<SysPermission> parentMenus = menuLevelMap.getOrDefault(parentLevel, Collections.emptyList());
            // 将子菜单挂到父菜单下
            attachChildrenToParent(menuList, parentMenus);
            for (SysPermission permission : menuList) {
                if (permission.getChecked() == 1 && CollUtil.isNotEmpty(permission.getChildren())) {
                    // 选中状态的子菜单
                    int checkedChildren = permission.getChildren().stream()
                            .filter(child -> child.getChecked() == 1)
                            .mapToInt(SysPermission::getChecked)
                            .sum();
                    // 如果选中子菜单数量不等于子菜单总数，则设置父菜单为半选中状态
                    if (checkedChildren != permission.getChildren().size()) {
                        permission.setChecked(2);
                    }
                }
            }
            result = menuList;
        }

        // 返回顶级菜单，即一级菜单
        return result;
    }

    /**
     * 将权限数据按菜单级别分组
     *
     * @param permissionList 权限数据列表
     * @return 按菜单级别分组的 Map
     */
    private Map<Integer, List<SysPermission>> groupPermissionsByLevel(List<SysPermission> permissionList) {
        Map<Integer, List<SysPermission>> map = new HashMap<>();
        for (SysPermission sysPermission : permissionList) {
            map.computeIfAbsent(sysPermission.getLev(), k -> new ArrayList<>()).add(sysPermission);
        }
        return map;
    }


    /**
     * 将子菜单挂到父菜单下
     *
     * @param children 子菜单列表
     * @param parents  父菜单列表
     */
    private void attachChildrenToParent(List<SysPermission> children, List<SysPermission> parents) {
        if (CollectionUtil.isEmpty(children) || CollectionUtil.isEmpty(parents)) {
            return;
        }
        // 将父菜单列表转换为 Map，方便快速查找
        Map<String, SysPermission> parentMap = parents.stream()
                .collect(Collectors.toMap(SysPermission::getPermissionCode, Function.identity()));

        // 遍历子菜单，挂到对应的父菜单下
        for (SysPermission child : children) {
            String parentKey = child.getParentPermissionCode();
            if (parentMap.containsKey(parentKey)) {
                SysPermission parent = parentMap.get(parentKey);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(child);
            }
        }
    }

    @CacheEvict(cacheNames = CacheableKey.ROLE_PERMISSION, allEntries = true)
    @Transactional
    @Override
    public RespWrapper<Boolean> savePermission(SysPermission permission) {
        log.info("savePermission {}", permission);
        if (permission == null || StrUtil.isBlank(permission.getPermissionCode()) || StrUtil.isBlank(permission.getPermissionName())) {
            log.error("savePermission fail, {}", permission);
            return RespWrapper.validateFailed("保存失败，缺少必要参数");
        }

        SysPermissionDo permissionDo = sysPermissionMapper.findByPermissionCode(permission.getPermissionCode());
        int resultNum;
        if (StrUtil.isNotBlank(permission.getId())) {
            // 更新
            if (null != permissionDo && !StrUtil.equals(permission.getId(), permissionDo.getId()) && StrUtil.equals(permission.getPermissionCode(), permissionDo.getPermissionCode())) {
                return RespWrapper.validateFailed("更新失败，权限编码已存在");
            }
            SysPermissionDo p = sysPermissionMapper.selectById(permission.getId());
            if (null == p) {
                return RespWrapper.validateFailed("更新失败，菜单信息不存在！");
            }
            String oldPermissionCode = p.getPermissionCode();
            p = SysPermissionConvert.INSTANCE.toDo(permission);
            resultNum = sysPermissionMapper.updateSysPermissionById(p);

            // 菜单变更
            if(!StrUtil.equals(permission.getPermissionCode(), oldPermissionCode)){
                // 更新子菜单数据
                sysPermissionMapper.updateParentPermissionCode(permission.getPermissionCode(), oldPermissionCode);
            }
        } else {
            // 新增
            if (null != permissionDo && StrUtil.equals(permission.getPermissionCode(), permissionDo.getPermissionCode())) {
                return RespWrapper.validateFailed("保存失败，权限编码已存在");
            }
            SysPermissionDo p = SysPermissionConvert.INSTANCE.toDo(permission);
            p.setId(String.valueOf(IdWorker.getId("t_sys_permission")));
            p.setCreateTime(DateUtil.date());
            p.setCreateBy(Identify.getSessionUser().getUserId());
            resultNum = sysPermissionMapper.insert(p);
        }
        log.info("savePermission {},resultNum={}", permission, resultNum);
        if (resultNum == 0) {
            return RespWrapper.fail("保存失败，缺少必要参数");
        }
        return RespWrapper.success(Boolean.TRUE);
    }

    @Override
    @CacheEvict(cacheNames = CacheableKey.ROLE_PERMISSION, allEntries = true)
    public RespWrapper<Boolean> deletePermission(String permissionId) {
        log.info("deletePermission permissionId={}", permissionId);
        SysPermissionDo sysPermissionDo = sysPermissionMapper.selectById(permissionId);
        List<SysPermissionDo> children = sysPermissionMapper.findByParentPermissionCode(sysPermissionDo.getPermissionCode());
        if (CollUtil.isNotEmpty(children)) {
            return RespWrapper.validateFailed("因为还有下级菜单，无法执行删除操作！");
        }
        List<SysRolePermissionDo> list = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermissionDo>()
                        .eq(SysRolePermissionDo::getPermissionId, permissionId)
        );
        if (CollUtil.isNotEmpty(list)) {
            int ret = sysRolePermissionMapper.delete(
                    new LambdaUpdateWrapper<SysRolePermissionDo>()
                            .eq(SysRolePermissionDo::getPermissionId, permissionId)
            );
            if (ret == 0) {
                log.error("resetPwd, sysRolePermissionMapper.delete fail, permissionId={}", permissionId);
                return RespWrapper.fail("删除失败！");
            }
        }
        if (sysPermissionMapper.deleteById(permissionId) == 0) {
            log.error("resetPwd, sysPermissionMapper.deleteById fail, permissionId={}", permissionId);
            return RespWrapper.fail("删除失败！");
        }
        return RespWrapper.success(Boolean.TRUE);
    }

    @Override
    public List<SysPermission> getSysPermissionByRoleId(String roleId) {
        List<SysPermission> sysPermissionDos = sysPermissionMapper.findAllByRoleId(roleId);
        if (CollUtil.isNotEmpty(sysPermissionDos)) {
            return getPermissionTree(sysPermissionDos);
        }
        return Collections.emptyList();
    }
}
