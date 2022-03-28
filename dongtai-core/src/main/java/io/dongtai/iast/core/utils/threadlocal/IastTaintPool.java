package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.log.DongTaiLog;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastTaintPool extends ThreadLocal<HashSet<Object>> {

    private static final PropertyUtils PROPERTIES = PropertyUtils.getInstance();

    @Override
    protected HashSet<Object> initialValue() {
        return null;
    }

    /**
     * 将待加入污点池中的数据插入到污点池，其中：复杂数据结构需要拆分为简单的数据结构
     * <p>
     * 检测类型，如果是复杂类型，将复杂类型转换为简单类型仅从保存 source点 获取的数据，需要将复杂类型的数据转换为简单类型进行存储
     * <p>
     * fixme: 后续补充所有类型
     *
     * @param obj source点的污点
     */
    public void addTaintToPool(Object obj, MethodEvent event, boolean isSource) {
        try{
            int subHashCode = 0;
            if (obj instanceof String[]) {
                this.get().add(obj);
                event.addTargetHash(obj.hashCode());

                String[] tempObjs = (String[]) obj;
                if (PROPERTIES.isNormalMode()) {
                    for (String tempObj : tempObjs) {
                        this.get().add(tempObj);
                        subHashCode = System.identityHashCode(tempObj);
                        EngineManager.TAINT_HASH_CODES.get().add(subHashCode);
                        event.addTargetHash(subHashCode);
                    }
                } else {
                    for (String tempObj : tempObjs) {
                        this.get().add(tempObj);
                        event.addTargetHash(tempObj.hashCode());
                    }
                }
            } else if (obj instanceof Map) {
                this.get().add(obj);
                event.addTargetHash(obj.hashCode());
                if (isSource) {
                    Map<String, String[]> tempMap = (Map<String, String[]>) obj;
                    Set<Map.Entry<String, String[]>> entries = tempMap.entrySet();
                    for (Map.Entry<String, String[]> entry : entries) {
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        addTaintToPool(key, event, true);
                        addTaintToPool(value, event, true);
                    }
                }
            } else if (obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive()) {
                Object[] tempObjs = (Object[]) obj;
                if (tempObjs.length != 0) {
                    for (Object tempObj : tempObjs) {
                        addTaintToPool(tempObj, event, isSource);
                    }
                }
            } else {
                this.get().add(obj);
                if (obj instanceof String && PROPERTIES.isNormalMode()) {
                    subHashCode = System.identityHashCode(obj);
                    EngineManager.TAINT_HASH_CODES.get().add(subHashCode);
                } else {
                    subHashCode = obj.hashCode();
                }
                event.addTargetHash(subHashCode);

            }
        }catch (Exception e){
            DongTaiLog.debug(e);
        }
    }

    public void addToPool(Object obj) {
        this.get().add(obj);
    }

    public boolean isEmpty() {
        return this.get().isEmpty();
    }

    public boolean isNotEmpty() {
        return !this.get().isEmpty();
    }
}
