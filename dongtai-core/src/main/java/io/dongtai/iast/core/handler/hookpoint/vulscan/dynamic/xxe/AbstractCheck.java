package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCheck implements XXEChecker {
    private static final String ORG_APACHE_XERCES_INTERNAL_IMPL_XMLENTITY_MANAGER = " org.apache.xerces.internal.impl.XMLEntityManager".substring(1);
    private static final String ORG_APACHE_XERCES_IMPL_XMLENTITY_MANAGER = " org.apache.xerces.impl.XMLEntityManager".substring(1);

    private static final String COM_SUN_ORG_APACHE_XERCES_INTERNAL_UTILS_SECURITY_SUPPORT = "com.sun.org.apache.xerces.internal.utils.SecuritySupport";
    private static final String COM_SUN_ORG_APACHE_XALAN_INTERNAL_UTILS_SECURITY_SUPPORT = "com.sun.org.apache.xalan.internal.utils.SecuritySupport";
    private static final String COM_SUN_ORG_APACHE_XERCES = "com.sun.org.apache.xerces";
    private static final String COM_SUN_ORG_APACHE_XALAN = "com.sun.org.apache.xalan";

    protected Object sourceObject;
    protected Object[] sourceParameters;

    @Override
    public void setSourceObjectAndParameters(Object sourceObject, Object[] sourceParameters) {
        this.sourceObject = sourceObject;
        this.sourceParameters = sourceParameters;
    }

    @Override
    public List<Object> getCheckObjects() {
        return Collections.singletonList(this.sourceObject);
    }

    @Override
    public abstract boolean match(Object obj);

    @Override
    public abstract Support getSupport(Object obj);

    public Object getXMLConfiguration(Object obj) {
        try {
            Field fConfigurationField = ReflectUtils.getRecursiveField(obj.getClass(), "fConfiguration");
            if (fConfigurationField == null) {
                return null;
            }

            return fConfigurationField.get(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied to get XMLConfiguration {}", e);
            return null;
        }
    }

    public Object getXMLEntityManager(Object obj) {
        try {
            Field fEntityManagerField = ReflectUtils.getDeclaredFieldFromSuperClassByName(obj.getClass(), "fEntityManager");
            if (fEntityManagerField == null) {
                return null;
            }
            return fEntityManagerField.get(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied to get XMLEntityManager {}", e);
            return null;
        }
    }

    public Support getXMLParserSupport(Object obj) {
        if (!isSupport(Feature.EXTERNAL_GENERAL, obj) && !isSupport(Feature.EXTERNAL_PARAMETER, obj)
                && !isSupport(Feature.LOAD_EXTERNAL_DTD, obj) && !isSupport(Feature.XINCLUDE_AWARE, obj)) {
            return Support.DISALLOWED;
        }

        try {
            Support support = getXMLParserConfigurationFeature(obj);
            if (support == Support.DISALLOWED) {
                return support;
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to read XMLReader fConfiguration to determine supports external entities {}", e);
            return Support.ALLOWED;
        }

        try {
            Support support = getXMLParserDocumentSourceFeature(obj);
            if (support == Support.DISALLOWED) {
                return getXMLParserContentHandlerFeature(obj);
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to read XMLReader fDocumentSource/fContentHandler to determine supports external entities {}", e);
        }

        Object fConfiguration = getXMLConfiguration(obj);
        if (fConfiguration != null) {
            String fAccessExternalDTD = getFeatureAccessExternalDTDFromSecurityPropertyManager(obj);
            Support support = getEntityManagerSupport(fConfiguration, fAccessExternalDTD);
            if (support == Support.DISALLOWED) {
                return support;
            }
        }

        return Support.ALLOWED;
    }

    private Support getXMLParserConfigurationFeature(Object obj) throws IllegalAccessException {
        Field fConfigurationField = ReflectUtils.getRecursiveField(obj.getClass(), "fConfiguration");
        Object fConfiguration = fConfigurationField != null ? fConfigurationField.get(obj) : null;
        if (fConfiguration != null && isSupport(Feature.DISALLOW_DOCTYPE, fConfiguration)
                && !isSupport(Feature.XINCLUDE_AWARE, fConfiguration)) {
            return Support.DISALLOWED;
        }
        return Support.ALLOWED;
    }

    private Support getXMLParserDocumentSourceFeature(Object obj) throws IllegalAccessException {
        Field fAccessExternalDTDField;
        Field fDocumentSourceField = ReflectUtils.getRecursiveField(obj.getClass(), "fDocumentSource");
        Object fDocumentSource = fDocumentSourceField != null ? fDocumentSourceField.get(obj) : null;
        if (fDocumentSource != null
                && (fAccessExternalDTDField = ReflectUtils.getRecursiveField(fDocumentSource.getClass(), "fAccessExternalDTD")) != null) {
            Object fAccessExternalDTD = fAccessExternalDTDField.get(fDocumentSource);
            if (fAccessExternalDTD instanceof String) {
                if ("".equals(fAccessExternalDTD)) {
                    return Support.DISALLOWED;
                }
            }
        }
        return Support.ALLOWED;
    }

    private Support getXMLParserContentHandlerFeature(Object obj) throws IllegalAccessException {
        Object xsltc = null;
        Field fContentHandlerField = ReflectUtils.getRecursiveField(obj.getClass(), "fContentHandler");
        Object fContentHandler = fContentHandlerField != null ? fContentHandlerField.get(obj) : null;
        if (fContentHandler == null) {
            return Support.ALLOWED;
        }
        Field xsltcField = ReflectUtils.getRecursiveField(fContentHandler.getClass(), "_xsltc");
        if (xsltcField != null) {
            xsltc = xsltcField.get(fContentHandler);
        }
        if (xsltc == null) {
            return Support.DISALLOWED;
        }
        Field accessExternalStylesheetField = ReflectUtils.getRecursiveField(xsltc.getClass(), "_accessExternalStylesheet");
        if (accessExternalStylesheetField != null && "".equals(accessExternalStylesheetField.get(xsltc))) {
            return Support.DISALLOWED;
        }
        return Support.ALLOWED;
    }

    public Support getEntityManagerSupport(Object fConfiguration, String fAccessExternalDTD) {
        Object fEntityManager = getXMLEntityManager(fConfiguration);
        if (fEntityManager == null) {
            return Support.UNKNOWN;
        }

        boolean externalGeneralEntitiesSupport = isSupport(Feature.EXTERNAL_GENERAL, fConfiguration);
        boolean externalParameterEntitiesSupport = isSupport(Feature.EXTERNAL_PARAMETER, fConfiguration);
        boolean loadExternalDTDSupport = isSupport(Feature.LOAD_EXTERNAL_DTD, fConfiguration);
        Support supportDTDSupport = Support.getSupport(fEntityManager, "fSupportDTD");

        if (!supportDTDSupport.isSupport()) {
            return Support.DISALLOWED;
        }

        if (!externalGeneralEntitiesSupport && !externalParameterEntitiesSupport && !loadExternalDTDSupport) {
            return Support.DISALLOWED;
        }

        if (!getSecurityAccessSupport(fEntityManager, fAccessExternalDTD).isSupport()) {
            if (!loadExternalDTDSupport) {
                return Support.ALLOWED;
            }
            return Support.DISALLOWED;
        }

        return Support.ALLOWED;
    }

    public Object getFeatureAccessExternalDTD(Object obj) {
        try {
            Field fAccessExternalDTDField = ReflectUtils.getRecursiveField(obj.getClass(), "fAccessExternalDTD");
            if (fAccessExternalDTDField == null) {
                return null;
            }
            return fAccessExternalDTDField.get(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied to get fAccessExternalDTDField {}", e);
            return null;
        }
    }

    String getFeatureAccessExternalDTDFromSecurityPropertyManager(Object obj) {
        try {
            String fAccessExternalDTD;
            Method getPropertyMethod = ReflectUtils.getDeclaredMethodFromClass(obj.getClass(), "getProperty", new Class<?>[]{String.class});
            if (getPropertyMethod != null) {
                Object securityPropertyManager = getPropertyMethod.invoke(obj, "http://www.oracle.com/xml/jaxp/properties/xmlSecurityPropertyManager");
                Method getValueMethod = ReflectUtils.getPublicMethodFromClass(securityPropertyManager.getClass(), "getValue", new Class<?>[]{String.class});
                fAccessExternalDTD = (String) getValueMethod.invoke(securityPropertyManager, Feature.ACCESS_EXTERNAL_DTD.getDtd()[0]);
                return fAccessExternalDTD;
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied when get fAccessExternalDTD from securityPropertyManager {}", e);
        } catch (NoSuchMethodException e) {
            DongTaiLog.debug("No method to get fAccessExternalDTD from securityPropertyManager {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Problem when get fAccessExternalDTD from securityPropertyManager {}", e);
        }
        return "all";
    }

    private Support getSecurityAccessSupport(Object obj, String fAccessExternalDTD) {
        String className;
        Support support = Support.UNKNOWN;
        Class<?> cls = obj.getClass();
        String name = cls.getName();
        if (name.endsWith(ORG_APACHE_XERCES_INTERNAL_IMPL_XMLENTITY_MANAGER)
                || name.endsWith(ORG_APACHE_XERCES_IMPL_XMLENTITY_MANAGER)) {
            if (name.startsWith(COM_SUN_ORG_APACHE_XALAN)) {
                className = COM_SUN_ORG_APACHE_XALAN_INTERNAL_UTILS_SECURITY_SUPPORT;
            } else if (name.startsWith(COM_SUN_ORG_APACHE_XERCES)) {
                className = COM_SUN_ORG_APACHE_XERCES_INTERNAL_UTILS_SECURITY_SUPPORT;
            } else {
                DongTaiLog.debug("Couldn't guess SecuritySupport class name based on entity manager {}", name);
                return support;
            }
            DongTaiLog.debug("Invoking security class {}", className);
            ClassLoader loader = cls.getClassLoader();
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
            }

            try {
                Class<?> loadClass = loader.loadClass(className);
                Class<?>[] ps = new Class[]{String.class, String.class, String.class};
                String checkAccess = (String) ReflectUtils.getDeclaredMethodFromClass(loadClass, "checkAccess", ps)
                        .invoke(null, "file:///etc/issue", fAccessExternalDTD, "all");
                Boolean fISCreatedByResolver = (Boolean) ReflectUtils.getDeclaredFieldFromClassByName(cls, "fISCreatedByResolver")
                        .get(obj);
                if (fISCreatedByResolver != null && !fISCreatedByResolver && checkAccess != null) {
                    return Support.DISALLOWED;
                }
                return Support.ALLOWED;
            } catch (Throwable e) {
                return Support.ALLOWED;
            }
        }
        return Support.UNKNOWN;
    }

    public static boolean isSupport(Feature feature, Object obj) {
        return feature.isSupport(getFeatureSupport(feature.getDtd(), obj));
    }

    public static Support getFeatureSupport(Object[] dtd, Object obj) {
        Support support = Support.UNKNOWN;
        try {
            Method method = ReflectUtils.getPublicMethodFromClass(obj.getClass(), "getFeature", new Class[]{String.class});
            Object f = method.invoke(obj, dtd);
            if (f instanceof Boolean) {
                support = (Boolean) f ? Support.ALLOWED : Support.DISALLOWED;
            }
        } catch (NoSuchMethodException e) {
            DongTaiLog.debug("failed to find getFeature() on {}", obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied when call getFeature() {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Problem call getFeature() {}", e);
        }
        return support;
    }

    public Support getPropertySupport(Object obj) {
        Method getPropertyMethod = ReflectUtils.getDeclaredMethodFromSuperClass(obj.getClass(), "getProperty", new Class<?>[]{String.class});
        if (getPropertyMethod == null) {
            return Support.ALLOWED;
        }
        if (!invokeMethod(getPropertyMethod, obj, " javax.xml.stream.supportDTD".substring(1), true)) {
            return Support.DISALLOWED;
        }
        if (!invokeMethod(getPropertyMethod, obj, " javax.xml.stream.isSupportingExternalEntities".substring(1), true)) {
            return Support.DISALLOWED;
        }
        return Support.ALLOWED;
    }

    private static boolean invokeMethod(Method method, Object obj, String parameter, boolean defaultVal) {
        try {
            Object invoke = method.invoke(obj, parameter);
            if (invoke instanceof Boolean) {
                return (Boolean) invoke;
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to access method {} {}", method.getName(), e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Failed to call {} {} {}", method.getName(), parameter, e);
        }
        return defaultVal;
    }
}
