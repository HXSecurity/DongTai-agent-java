package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.*;

public class XMLInputFactoryCheck extends AbstractCheck {
    @Override
    public boolean match(Object obj) {
        return obj != null && ReflectUtils.isDescendantOf(obj.getClass(), " javax.xml.stream.XMLInputFactory".substring(1));
    }

    @Override
    public Support getSupport(Object obj) {
        Class<?> cls = obj.getClass();
        if (cls.getName().contains(".ImmutableXMLInputFactory")) {
            Object normalizeWrapper = getParentObjectHasSuffix(obj, ".NormalizingXMLInputFactoryWrapper");
            if (normalizeWrapper != null
                    && getParentObjectHasSuffix(normalizeWrapper, ".DisallowDoctypeDeclInputFactoryWrapper") != null) {
                return Support.DISALLOWED;
            }
            return Support.ALLOWED;
        }
        try {
            Method isPropertySupportedMethod = ReflectUtils.getDeclaredMethodFromClass(cls, "isPropertySupported", new Class[]{String.class});
            Method getPropertyMethod = ReflectUtils.getDeclaredMethodFromClass(cls, "getProperty", new Class[]{String.class});

            boolean supportDTD = invokeXMLInputFactoryMethod(isPropertySupportedMethod, getPropertyMethod, obj,
                    " javax.xml.stream.supportDTD".substring(1));
            if (!supportDTD) {
                return Support.DISALLOWED;
            }

            boolean isSupportingExternalEntities = invokeXMLInputFactoryMethod(isPropertySupportedMethod,
                    getPropertyMethod, obj, " javax.xml.stream.isSupportingExternalEntities".substring(1));
            if (!isSupportingExternalEntities) {
                return Support.DISALLOWED;
            }

            String fAccessExternalDTD = getFeatureAccessExternalDTDFromSecurityPropertyManager(obj);
            if ("".equals(fAccessExternalDTD)) {
                return Support.DISALLOWED;
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Couldn't get access to confirm whether XMLInputFactory supported external entities {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Couldn't confirm whether XMLInputFactory supported external ent {}", e);
        }
        return Support.ALLOWED;
    }

    private Object getParentObjectHasSuffix(Object obj, String str) {
        Field parentField = ReflectUtils.getDeclaredFieldFromSuperClassByName(obj.getClass(), "parent");
        if (parentField != null) {
            try {
                Object parent = parentField.get(obj);
                if (parent != null) {
                    if (parent.getClass().getName().endsWith(str)) {
                        return parent;
                    }
                }
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    private boolean invokeXMLInputFactoryMethod(Method isPropertySupportedMethod, Method getPropertyMethod,
                                                       Object obj, String parameter)
            throws InvocationTargetException, IllegalAccessException {
        if (isPropertySupportedMethod == null || getPropertyMethod == null) {
            return true;
        }
        Object isPropertySupported = isPropertySupportedMethod.invoke(obj, parameter);
        if ((isPropertySupported instanceof Boolean) && (Boolean) isPropertySupported) {
            Object property = getPropertyMethod.invoke(obj, parameter);
            if (property instanceof Boolean) {
                return (Boolean) property;
            }
        }
        return true;
    }
}
