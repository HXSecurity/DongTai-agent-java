package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ObjectShare;
import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XMLUnmarshallerCheck extends AbstractCheck {
    private final static String JAVAX_XML_BIND_UNMARSHALLER = " javax.xml.bind.Unmarshaller".substring(1);
    private static final String JAVAX_XML_TRANSFORM_SAX_SAXSOURCE = " javax.xml.transform.sax.SAXSource".substring(1);

    @Override
    public boolean match(Object obj) {
        boolean isMatch = false;
        if (obj == null) {
            return false;
        }
        try {
            isMatch = obj.getClass().isAssignableFrom(Class.forName(JAVAX_XML_BIND_UNMARSHALLER));
        } catch (ClassNotFoundException e) {
        }
        if (!isMatch) {
            return obj.getClass().getName().endsWith(".UnmarshallerImpl");
        }
        return true;
    }

    @Override
    public Support getSupport(Object obj) {
        Class<?> cls;
        cls = obj.getClass();
        Object xmlReader = getUnmarshallerXMLReader(this.sourceParameters);
        if (xmlReader != null) {
            return getXMLParserSupport(xmlReader);
        }
        Object reader = getUnmarshallerXMLReader(obj, cls);
        if (reader != null) {
            return getUnmarshallerXMLReaderSupport(reader, cls);
        }
        return Support.ALLOWED;
    }

    private Support getUnmarshallerXMLReaderSupport(Object obj, Class<?> cls) {
        if (!getUnmarshallerPropertySupport(obj).isSupport()) {
            return Support.DISALLOWED;
        }
        Object fConfiguration = getXMLConfiguration(obj);
        if (fConfiguration == null) {
            DongTaiLog.debug("fConfiguration field was null", cls);
            return Support.ALLOWED;
        }

        String fAccessExternalDTD = getFeatureAccessExternalDTDFromSecurityPropertyManager(obj);
        return getEntityManagerSupport(fConfiguration, fAccessExternalDTD);
    }

    private Support getUnmarshallerPropertySupport(Object obj) {
        try {
            Method getPropertyMethod = ReflectUtils.getPublicMethodFromClass(obj.getClass(), "getProperty", new Class<?>[]{String.class});
            Object invoke = getPropertyMethod.invoke(obj, Feature.ACCESS_EXTERNAL_DTD.getDtd()[0]);
            if (invoke instanceof String) {
                if (((String) invoke).isEmpty()) {
                    return Support.DISALLOWED;
                }
                return Support.ALLOWED;
            }
        } catch (NoSuchMethodException e) {
            DongTaiLog.debug("Failed to find Unmarshaller getProperty() {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Problem reflecting Unmarshaller getProperty() {}", e);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied when reflecting Unmarshaller getProperty() {}", e);
        }
        return Support.UNKNOWN;
    }

    private Object getUnmarshallerXMLReader(Object[] parameters) {
        if (parameters == null) {
            return null;
        }
        for (Object parameter : parameters) {
            if (parameter == null) {
                continue;
            }

            Class<?> clazz = parameter.getClass();
            String str = clazz.getName();
            if (!JAVAX_XML_TRANSFORM_SAX_SAXSOURCE.equals(str)) {
                continue;
            }

            try {
                Method method = ReflectUtils.getPublicMethodFromClass(clazz, "getXMLReader");
                return method.invoke(parameter, (Object[]) null);
            } catch (IllegalAccessException e) {
                DongTaiLog.debug("Failed to get access to getXMLReader method on SAXSource {}", e);
            } catch (InvocationTargetException e) {
                DongTaiLog.debug("Failed to invoke getXMLReader method on SAXSource {}", e);
            } catch (NoSuchMethodException e) {
                DongTaiLog.debug("Failed to find getXMLReader method on SAXSource {}", e);
            }
        }
        return null;
    }

    private Object getUnmarshallerXMLReader(Object obj, Class<?> cls) {
        Method method = ReflectUtils.getDeclaredMethodFromSuperClass(cls, "getXMLReader", ObjectShare.EMPTY_CLASS_ARRAY);
        if (method == null) {
            DongTaiLog.debug("Couldn't find getXMLReader method from {}", cls);
            return null;
        }

        try {
            Object invoke = method.invoke(obj, ObjectShare.EMPTY_OBJ_ARRAY);
            if (invoke != null) {
                return invoke;
            }
            DongTaiLog.debug("Encountered null Unmarshaller#getXMLReader");
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to get access to getXMLReader method on Unmarshaller {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Failed to invoke getXMLReader method on Unmarshaller {}", e);
        }
        return null;
    }

}
