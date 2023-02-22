package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class XMLStreamReaderCheck extends AbstractCheck {
    @Override
    public List<Object> getCheckObjects() {
        List<Object> objs = new ArrayList<Object>();
        if (this.sourceParameters.length > 0 && this.sourceParameters[0] != null) {
            objs.add(this.sourceParameters[0]);
        }
        objs.add(this.sourceObject);
        return objs;
    }

    @Override
    public boolean match(Object obj) {
        return obj.getClass().getName().contains(".XMLStreamReader");
    }

    @Override
    public Support getSupport(Object obj) {
        Object fEntityManager = getXMLEntityManager(obj);
        if (fEntityManager != null) {
            Object fAccessExternalDTD = getFeatureAccessExternalDTD(fEntityManager);
            if ((fAccessExternalDTD instanceof String) && ((String) fAccessExternalDTD).isEmpty()) {
                return Support.DISALLOWED;
            }
        }

        try {
            Field fPropertyManagerField = ReflectUtils.getDeclaredFieldFromClassByName(obj.getClass(), "fPropertyManager");
            if (fPropertyManagerField == null) {
                return Support.ALLOWED;
            }

            return getPropertySupport(fPropertyManagerField.get(obj));
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to access fPropertyManager {}", e);
            return Support.ALLOWED;
        }
    }
}
