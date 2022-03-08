package io.dongtai.iast.core.bytecode.enhance.plugin.spring;

import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.handler.hookpoint.api.GetApiThread;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;

/**
 * niuerzhuang@huoxian.cn
 */
public class SpringApplicationImpl {

    private static IastClassLoader iastClassLoader;
    public static Method getAPI;
    public static boolean isSend;

    public static void getWebApplicationContext(MethodEvent event) {
        if (!isSend) {
            Object applicationContext = event.returnValue;
            createClassLoader(applicationContext);
            loadApplicationContext();
            GetApiThread getApiThread = new GetApiThread(applicationContext);
            getApiThread.start();
        }
    }

    private static void createClassLoader(Object applicationContext) {
        try {
            if (iastClassLoader == null) {
                iastClassLoader = HttpImpl.getClassLoader();
            }
        } catch (Exception e) {
            DongTaiLog.error(e.getMessage());
        }
    }

    private static void loadApplicationContext() {
        if (getAPI == null) {
            try {
                Class<?> proxyClass;
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.spring.SpringApplicationContext");
                getAPI = proxyClass.getDeclaredMethod("getAPI", Object.class);
            } catch (NoSuchMethodException e) {
                DongTaiLog.error(e.getMessage());
            }
        }
    }

}
