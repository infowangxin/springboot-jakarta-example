package com.nutcracker.example.demo.service.auth.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nutcracker.example.demo.constant.DemoConstants;
import com.nutcracker.example.demo.convert.auth.SysPermissionConvert;
import com.nutcracker.example.demo.entity.ApiResponse;
import com.nutcracker.example.demo.entity.dataobject.auth.SysPermissionDo;
import com.nutcracker.example.demo.entity.domain.auth.SessionUser;
import com.nutcracker.example.demo.entity.domain.auth.SysPermission;
import com.nutcracker.example.demo.mapper.auth.SysPermissionMapper;
import com.nutcracker.example.demo.service.auth.SysPermissionService;
import com.nutcracker.example.demo.util.JSON;
import com.nutcracker.example.demo.web.Identify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    @Override
    public List<SysPermission> findAllSysPermission() {
        List<SysPermissionDo> sysPermissionDos = sysPermissionMapper.findAllSysPermission();
        log.info("findAllSysPermission: \n{}", JSON.toJSONString(sysPermissionDos));
        return getPermissionTree(sysPermissionDos);
    }

    @Override
    public List<SysPermission> getMenuPermissionByUserId(String userId) {
        List<SysPermissionDo> sysPermissionDos = sysPermissionMapper.findPermissionByUserId(userId);
        log.info("getMenuPermissionByUserId: \n{}", JSON.toJSONString(sysPermissionDos));
        return getPermissionTree(sysPermissionDos);
    }

    private List<SysPermission> getPermissionTree(List<SysPermissionDo> sysPermissionDos) {
        // 如果查询结果为空，直接返回空列表
        if (CollectionUtil.isEmpty(sysPermissionDos)) {
            return Collections.emptyList();
        }

        // 将 DO 对象转换为 VO 对象，并按菜单级别分组
        Map<Integer, List<SysPermission>> menuLevelMap = groupPermissionsByLevel(sysPermissionDos);

        // 获取各级菜单
        List<SysPermission> oneLevelMenus = menuLevelMap.getOrDefault(1, Collections.emptyList());
        List<SysPermission> twoLevelMenus = menuLevelMap.getOrDefault(2, Collections.emptyList());
        List<SysPermission> threeLevelMenus = menuLevelMap.getOrDefault(3, Collections.emptyList());

        // 将三级菜单挂到二级菜单下
        attachChildrenToParent(threeLevelMenus, twoLevelMenus);

        // 将二级菜单挂到一级菜单下
        attachChildrenToParent(twoLevelMenus, oneLevelMenus);

        // 返回一级菜单
        return oneLevelMenus;
    }

    /**
     * 将权限数据按菜单级别分组
     *
     * @param sysPermissionDos 权限数据列表
     * @return 按菜单级别分组的 Map
     */
    private Map<Integer, List<SysPermission>> groupPermissionsByLevel(List<SysPermissionDo> sysPermissionDos) {
        Map<Integer, List<SysPermission>> map = new HashMap<>();
        for (SysPermissionDo sysPermissionDo : sysPermissionDos) {
            SysPermission sysPermission = SysPermissionConvert.INSTANCE.toDomain(sysPermissionDo);
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

    @Transactional
    @Override
    public ApiResponse<Boolean> savePermission(SysPermission sysPermission) {
        log.info("savePermission {}", sysPermission);
        if (sysPermission == null || StrUtil.isBlank(sysPermission.getPermissionCode()) || StrUtil.isBlank(sysPermission.getPermissionName())) {
            log.error("savePermission fail, {}", sysPermission);
            return ApiResponse.fail("保存失败，缺少必要参数");
        }
        SysPermissionDo p = sysPermissionMapper.selectById(sysPermission.getId());
        int resultNum;
        if (p != null) {
            p = SysPermissionConvert.INSTANCE.toDo(sysPermission);
            resultNum = sysPermissionMapper.updateSysPermissionById(p);
        } else {
            SessionUser sessionUser = Identify.getSessionUser();
            p = SysPermissionConvert.INSTANCE.toDo(sysPermission);
            p.setId(String.valueOf(IdWorker.getId("t_sys_permission")));
            p.setCreateTime(LocalDateTime.now());
            p.setCreateBy(sessionUser.getRealName());
            resultNum = sysPermissionMapper.insert(p);
        }
        log.info("savePermission {},resultNum={}", sysPermission, resultNum);
        if (resultNum == 0) {
            return ApiResponse.fail("保存失败，缺少必要参数");
        }
        return ApiResponse.ofSuccess(Boolean.TRUE);
    }

    @Override
    public ApiResponse<Boolean> deletePermission(String id) {
        log.info("deletePermission id={}", id);
        SysPermissionDo sysPermissionDo = sysPermissionMapper.selectById(id);
        List<SysPermissionDo> children = sysPermissionMapper.findPermissionByParentPermissionCode(sysPermissionDo.getPermissionCode());
        if (CollUtil.isNotEmpty(children)) {
            return ApiResponse.fail("因为还有下级菜单，无法执行删除操作！");
        }
        if (sysPermissionMapper.deleteById(id) > 0) {
            return ApiResponse.ofSuccess(Boolean.TRUE);
        }
        return ApiResponse.fail("删除失败！");
    }

    @Override
    public PageInfo<SysPermission> findSysPermissionByPage(Integer pageNum, SysPermission permission) {
        log.info("findSysPermissionByPage , pageNum={},{}", pageNum, permission);
        pageNum = pageNum == null ? 1 : pageNum;
        PageHelper.startPage(pageNum, DemoConstants.PAGE_SIZE);
        List<SysPermission> list = sysPermissionMapper.findSysPermission(permission);
        PageInfo<SysPermission> page = new PageInfo<>(list);
        log.debug("findSysPermissionByPage page.toString()={}", page);
        return page;
    }

}
