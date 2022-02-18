package io.dongtai.iast.core.utils.threadlocal;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastTrackMap extends ThreadLocal<Map<Integer, MethodEvent>> {
    @Override
    protected Map<Integer, MethodEvent> initialValue() {
        return null;
    }

    public void addTrackMethod(Integer invokeId, MethodEvent event) {
        this.get().put(invokeId, event);
    }
}
