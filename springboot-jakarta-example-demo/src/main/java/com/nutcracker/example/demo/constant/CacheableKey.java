package com.nutcracker.example.demo.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 缓存时长
 *
 * @author 胡桃夹子
 * @date 2020-09-21 13:12
 */
public class CacheableKey {

    public static final String PREFIX = "nutcracker_";

    /** SysUser CacheManager */
    public static final String SYS_USER = PREFIX + "sys_user";
    /** 角色权限缓存 */
    public static final String ROLE_PERMISSION = PREFIX + "role_permission";

    /** 1天 */
    public static final long CACHE_1_DAY = 86400;
    /** 2小时 */
    public static final long CACHE_2_HOUR = 7200;

    private static final Map<String, Long> CACHEABLE_KEY_MAP = new LinkedHashMap<>();

    static {
        // 设置缓存时长
        CACHEABLE_KEY_MAP.put(SYS_USER, CACHE_2_HOUR);
        CACHEABLE_KEY_MAP.put(ROLE_PERMISSION, CACHE_1_DAY);
    }

    public static Map<String, Long> getCacheableKeyMap() {
        return CACHEABLE_KEY_MAP;
    }
}
