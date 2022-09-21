package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastTaintHashCodes extends ThreadLocal<HashSet<Integer>> {
    @Override
    protected HashSet<Integer> initialValue() {
        return null;
    }

    public boolean isEmpty() {
        return this.get() == null || this.get().isEmpty();
    }

    public boolean contains(Integer hashCode) {
        if (this.get() == null) {
            return false;
        }
        return this.get().contains(hashCode);
    }

    public void add(Integer hashCode) {
        if (this.get() == null) {
            return;
        }
        this.get().add(hashCode);
    }

    public void addObject(Object obj, MethodEvent event, boolean isSource) {
        if (!TaintPoolUtils.isNotEmpty(event.outValue)) {
            return;
        }

        try {
            int subHashCode = 0;
            if (obj instanceof String[]) {
                String[] tempObjs = (String[]) obj;
                for (String tempObj : tempObjs) {
                    subHashCode = System.identityHashCode(tempObj);
                    this.add(subHashCode);
                    event.addTargetHash(subHashCode);
                }
            } else if (obj instanceof Map) {
                int hashCode = System.identityHashCode(obj);
                this.add(hashCode);
                event.addTargetHash(hashCode);
                if (isSource) {
                    Map<String, String[]> tempMap = (Map<String, String[]>) obj;
                    Set<Map.Entry<String, String[]>> entries = tempMap.entrySet();
                    for (Map.Entry<String, String[]> entry : entries) {
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        this.addObject(key, event, true);
                        this.addObject(value, event, true);
                    }
                }
            } else if (obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive()) {
                Object[] tempObjs = (Object[]) obj;
                if (tempObjs.length != 0) {
                    for (Object tempObj : tempObjs) {
                        this.addObject(tempObj, event, isSource);
                    }
                }
            } else {
                subHashCode = System.identityHashCode(obj);
                this.add(subHashCode);
                event.addTargetHash(subHashCode);
            }
        } catch (Exception e) {
            DongTaiLog.error("add object to taint pool failed", e);
        }
    }
}
