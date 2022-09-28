package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;

public class XomCheck extends AbstractCheck {
    @Override
    public boolean match(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass().getName().endsWith("nu.xom.Builder");
    }

    @Override
    public Support getSupport(Object obj) {
        Object parser = getXomParser(obj);
        if (parser == null || isXomXml1(parser) || getXMLParserSupport(parser).isSupport()) {
            return Support.ALLOWED;
        }
        return Support.DISALLOWED;
    }

    private Object getXomParser(Object obj) {
        try {
            Field field = ReflectUtils.getRecursiveField(obj.getClass(), "parser");
            if (field == null) {
                return null;
            }
            return field.get(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Problem reflecting xom XMLReader", e);
            return null;
        }
    }

    private boolean isXomXml1(Object obj) {
        return obj.getClass().getName().contains("nu.xom.XML1");
    }
}
