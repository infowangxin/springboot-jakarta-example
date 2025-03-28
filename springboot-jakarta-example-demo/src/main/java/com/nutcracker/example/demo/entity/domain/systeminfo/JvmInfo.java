package com.nutcracker.example.demo.entity.domain.systeminfo;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Date;

/**
 * jvm info
 *
 * @author 胡桃夹子
 * @date 2022/12/22 08:57
 */
@Setter
public class JvmInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -3589474410065398808L;
    /**
     * 当前JVN占用的内存总数（M）
     */
    private double total;

    /**
     * JVM最大可用内存总数（M）
     */
    private double max;

    /**
     * JVM空闲内存（M）
     */
    private double free;

    /**
     * JDK版本
     */
    private String version;

    /**
     * JDK路径
     */
    private String home;

    /**
     * 获取JDK名称
     */
    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getVmName();
    }

    /**
     * JDK启动时间
     */
    public String getStartTime() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        Date date = new Date(time);
        return DateUtil.formatDateTime(date);
    }

    /**
     * JDK运行时间
     */
    public String getRunTime() {

        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        Date date = new Date(time);

        //运行多少分钟
        long runMS = DateUtil.between(date, new Date(), DateUnit.MS);

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;

        long day = runMS / nd;
        long hour = runMS % nd / nh;
        long min = runMS % nd % nh / nm;

        return day + "天" + hour + "小时" + min + "分钟";
    }

    public double getTotal() {
        return NumberUtil.div(total, (1024 * 1024), 2);
    }

    public double getMax() {
        return NumberUtil.div(max, (1024 * 1024), 2);
    }

    public double getFree() {
        return NumberUtil.div(free, (1024 * 1024), 2);
    }

    public double getUsed() {
        return NumberUtil.div(total - free, (1024 * 1024), 2);
    }

    public String getVersion() {
        return version;
    }

    public String getHome() {
        return home;
    }

    public double getUsage() {
        return NumberUtil.mul(NumberUtil.div(total - free, total, 4), 100);
    }
}
