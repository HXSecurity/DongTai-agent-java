package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

public enum Support {
    UNKNOWN,
    ALLOWED,
    DISALLOWED;

    public static io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe.Support getSupport(Object obj, String fieldName) {
        try {
            return getSupport(ReflectUtils.getFieldFromClass(obj.getClass(), fieldName).getBoolean(obj));
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to reflect XMLEntityManager field value {}", e);
            return UNKNOWN;
        } catch (NoSuchFieldException e) {
            return UNKNOWN;
        }
    }

    public static io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe.Support getSupport(boolean isSupport) {
        return isSupport ? ALLOWED : DISALLOWED;
    }

    public boolean isSupport() {
        return this != DISALLOWED;
    }
}
