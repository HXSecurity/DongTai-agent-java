package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractCheck {
    public Object getCheckObject(MethodEvent event) {
        return event.object;
    }

    public static boolean isSupport(Feature feature, Object obj) {
        return feature.isSupport(getFeatureSupport(feature.getDtd(), obj));
    }

    public static Support getFeatureSupport(Object[] dtd, Object obj) {
        Support support = Support.UNKNOWN;
        try {
            Method method = ReflectUtils.getPublicMethodFromClass(obj.getClass(), "getFeature", new Class[]{String.class});
            Object f = method.invoke(obj, dtd);
            if (f instanceof Boolean) {
                support = (Boolean) f ? Support.ALLOWED : Support.DISALLOWED;
            }
        } catch (NoSuchMethodException e) {
            DongTaiLog.debug("failed to find getFeature() on {}", obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied when call getFeature() {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Problem call getFeature() {}", e);
        }
        return support;
    }

    private static boolean invokeMethod(Method method, Object obj, String parameter, boolean defaultVal) {
        try {
            Object invoke = method.invoke(obj, parameter);
            if (invoke instanceof Boolean) {
                return (Boolean) invoke;
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.error("Failed to access method {} {}", method.getName(), e);

        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Failed to call {} {} {}", method.getName(), parameter, e);
        }
        return defaultVal;
    }
}
