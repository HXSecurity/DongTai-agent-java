package com.secnium.iast.core.util;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.models.MethodEvent;

import java.util.Iterator;
import java.util.Set;

/**
 * 检测污点池中是否存在目标对象
 * 解决方案，
 * 1.将加入污点池的复杂对象，拆分为简单对象，后续直接检测
 * 2.检测时，将污点池中的复杂对象拆分出来
 * <p>
 * 场景：污点池中数据的查询数多于插入数量
 */
public class TaintPoolUtils {
    private static final boolean enableAllHook = EngineManager.isEnableAllHook();

    public static boolean poolContains(Object obj, MethodEvent event) {
        if (obj == null) {
            return false;
        }

        boolean isContains = false;
        boolean isString = obj instanceof String;
        // 检查对象是否存在
        isContains = contains(obj, isString, event);
        if (!isContains) {
            if (obj instanceof String[]) {
                String[] tempObjs = (String[]) obj;
                for (String tempObj : tempObjs) {
                    isContains = contains(tempObj, true, event);
                    if (isContains) {
                        EngineManager.TAINT_POOL.addToPool(obj);
                        event.addSourceHash(obj.hashCode());
                        break;
                    }
                }
            }
        }
        return isContains;
    }

    private static boolean contains(Object obj, boolean isString, MethodEvent event) {
        Set<Object> taints = EngineManager.TAINT_POOL.get();
        Iterator<Object> iterator = taints.iterator();

        while (iterator.hasNext()) {
            try {
                Object value = iterator.next();
                if (isString) {
                    if (enableAllHook) {
                        if (obj == value) {
                            event.addSourceHash(System.identityHashCode(obj));
                            return true;
                        } else if (obj.equals(value)) {
                            // fixme 全量hook时，增加此逻辑
                            event.addSourceHash(System.identityHashCode(value));
                            return true;
                        }
                    } else if (obj == value) {
                        event.addSourceHash(System.identityHashCode(obj));
                        return true;
                    }
                } else {
                    if (obj.equals(value)) {
                        event.addSourceHash(obj.hashCode());
                        return true;
                    }
                }
            } catch (Throwable ignore) {

            }

        }
        return false;
    }

}
