package io.dongtai.iast.common.utils.limit;

import io.dongtai.log.DongTaiLog;

/**
 * @author mazepeng
 * @date 2023/12/5 14:11
 */
public class InterfaceRateLimiterUtil {


    private static volatile InterfaceRateLimiter instance;

    private static volatile boolean turnOnTheRateLimiter = false;

    private InterfaceRateLimiterUtil() {
    }

    /**
     * 获取接口速率限制器的状态
     * @return true 开启 false 关闭
     */
    public static boolean getRateLimiterState(){
        return turnOnTheRateLimiter;
    }

    public static void turnOffTheRateLimiter(boolean state){
        turnOnTheRateLimiter = true;
    }

    /**
     * 初始化速率限制
     */
    public static void initializeInstance(long rateCaps) {
        if (instance == null) {
            synchronized (InterfaceRateLimiterUtil.class) {
                if (instance == null) {
                    instance = InterfaceRateLimiter.getInstance(rateCaps);
                    turnOnTheRateLimiter = true;
                }
            }
        }
    }

    /**
     *  接口采集判断器
     * @param interfaceName 接口api名称
     * @return true 放行采集 false 拦截不采集
     * 默认是放行采集
     */
    public static boolean whetherItPassesOrNot(String interfaceName){
        if (instance == null){
            DongTaiLog.warn("请先初始化接口速率限制器");
            return true;
        }
        return instance.whetherItPassesOrNot(interfaceName);
    }

}

