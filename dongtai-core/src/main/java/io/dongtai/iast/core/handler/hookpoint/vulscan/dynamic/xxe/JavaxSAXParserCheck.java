package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;

public class JavaxSAXParserCheck extends AbstractCheck {
    @Override
    public boolean match(Object obj) {
        if (obj == null) {
            return false;
        }
        return ReflectUtils.isDescendantOf(obj.getClass().getSuperclass(), "javax.xml.parsers.SAXParser");
    }

    @Override
    public Support getSupport(Object obj) {
        Object xmlReader = getJavaxSAXParserXMLReader(obj);
        if (xmlReader == null) {
            return Support.ALLOWED;
        }
        if (isIBMWebServicesParser(obj, this.sourceParameters)) {
            return Support.DISALLOWED;
        }
        return getXMLParserSupport(xmlReader);
    }

    private Object getJavaxSAXParserXMLReader(Object obj) {
        try {
            return ReflectUtils.getPublicMethodFromClass(obj.getClass(), "getXMLReader").invoke(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to get access to getXMLReader method on SAXParser {}", e);
            return null;
        } catch (NoSuchMethodException e) {
            DongTaiLog.debug("Failed to find getXMLReader method on SAXParser {}", e);
            return null;
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Failed to invoke getXMLReader method on SAXParser {}", e);
            return null;
        }
    }

    private boolean isIBMWebServicesParser(Object obj, Object[] parameters) {
        return "com.ibm.ws.webservices.engine.utils.WebServicesParser".equals(obj.getClass().getName())
                && isIBMP2DConverter(parameters);
    }

    private boolean isIBMP2DConverter(Object[] parameters) {
        if (parameters == null || parameters.length < 2) {
            return false;
        }
        return "com.ibm.ws.webservices.engine.events.P2DConverter".equals(parameters[1] == null ? null : parameters[1].getClass().getName());
    }
}
