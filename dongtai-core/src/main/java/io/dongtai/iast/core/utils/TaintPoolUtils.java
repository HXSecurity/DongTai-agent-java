package io.dongtai.iast.core.utils;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import java.util.Iterator;
import java.util.Set;

/**
 * 检测污点池中是否存在目标对象 解决方案， 1.将加入污点池的复杂对象，拆分为简单对象，后续直接检测 2.检测时，将污点池中的复杂对象拆分出来
 * <p>
 * 场景：污点池中数据的查询数多于插入数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class TaintPoolUtils {

    public static boolean poolContains(Object obj, MethodEvent event) {
        if (obj == null) {
            return false;
        }

        boolean isContains;
        boolean isString = obj instanceof String;
        // 检查对象是否存在
        isContains = contains(obj, isString, event);
        if (!isContains) {
            if (obj instanceof String[]) {
                String[] stringArray = (String[]) obj;
                for (String stringItem : stringArray) {
                    isContains = contains(stringItem, true, event);
                    if (isContains) {
                        EngineManager.TAINT_POOL.addToPool(obj);
                        event.addSourceHash(obj.hashCode());
                        break;
                    }
                }
            }
            if (obj instanceof Object[]) {
                Object[] objArray = (Object[]) obj;
                for (Object objItem : objArray) {
                    if (poolContains(objItem, event)) {
                        return true;
                    }
                }
            }
        }
        return isContains;
    }

    /**
     * 判断污点是否匹配
     *
     * @param obj
     * @param isString
     * @param event
     * @return
     */
    private static boolean contains(Object obj, boolean isString, MethodEvent event) {
        Set<Object> taints = EngineManager.TAINT_POOL.get();
        int hashcode = 0;
        // 检查是否

        if (isString && PropertyUtils.getInstance().isNormalMode()) {
            Iterator<Object> iterator = taints.iterator();
            hashcode = System.identityHashCode(obj);
            while (iterator.hasNext()) {
                try {
                    Object value = iterator.next();
                    if (obj.equals(value)) {
                        // 检查当前污点的hashcode是否在hashcode池中，如果在，则标记传播
                        if (EngineManager.TAINT_HASH_CODES.get().contains(hashcode)) {
                            event.addSourceHash(hashcode);
                            return true;
                        }
                    }
                } catch (Throwable ignore) {

                }
            }
        } else {
            try {
                if (taints.contains(obj)) {
                    event.addSourceHash(obj.hashCode());
                    return true;
                }
            } catch (Throwable ignore) {

            }
            return false;
        }

        return false;
    }

}
