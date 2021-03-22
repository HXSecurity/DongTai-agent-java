package com.secnium.iast.core.threadlocalpool;

import com.secnium.iast.core.handler.models.MethodEvent;

import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IASTTrackMap extends ThreadLocal<Map<Integer, MethodEvent>> {
    @Override
    protected Map<Integer, MethodEvent> initialValue() {
        return null;
    }

    public void addTrackMethod(Integer invokeId, MethodEvent event) {
        this.get().put(invokeId, event);
    }
}
