package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.HashSet;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastTaintHashCodes extends ThreadLocal<HashSet<Long>> {
    @Override
    protected HashSet<Long> initialValue() {
        return null;
    }

    public boolean isEmpty() {
        return this.get() == null || this.get().isEmpty();
    }

    public boolean contains(Long hashCode) {
        if (this.get() == null) {
            return false;
        }
        return this.get().contains(hashCode);
    }

    public void add(Long hashCode) {
        if (this.get() == null) {
            return;
        }
        this.get().add(hashCode);
    }

    public void addObject(Object obj, MethodEvent event) {
        if (!TaintPoolUtils.isNotEmpty(obj) || !TaintPoolUtils.isAllowTaintType(obj)) {
            return;
        }

        try {
            long subHashCode = 0;
            if (obj instanceof String[]) {
                String[] tempObjs = (String[]) obj;
                for (String tempObj : tempObjs) {
                    subHashCode = TaintPoolUtils.toStringHash(tempObj.hashCode(),System.identityHashCode(tempObj));
                    this.add(subHashCode);
                    event.addTargetHash(subHashCode);
                }
            } else if (obj instanceof Map) {
                long hashCode = System.identityHashCode(obj);
                this.add(hashCode);
                event.addTargetHash(hashCode);
            } else if (obj instanceof String){
                long hashCode = TaintPoolUtils.toStringHash(obj.hashCode(),System.identityHashCode(obj));
                this.add(hashCode);
                event.addTargetHash(hashCode);
            } else if (obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive()) {
                Object[] tempObjs = (Object[]) obj;
                if (tempObjs.length != 0) {
                    for (Object tempObj : tempObjs) {
                        this.addObject(tempObj, event);
                    }
                }
            } else {
                subHashCode = System.identityHashCode(obj);
                this.add(subHashCode);
                event.addTargetHash(subHashCode);
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("UTIL_TAINT_ADD_OBJECT_TO_POOL_FAILED"), e);
        }
    }
}
