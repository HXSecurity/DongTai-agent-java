package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import javax.xml.parsers.DocumentBuilder;
import java.lang.reflect.*;

public class DocumentBuilderCheck extends AbstractCheck {
    private static final String ORACLE_XDKJAVA_SECURITY_RESOLVE_ENTITY_DEFAULT = "oracle.xdkjava.security.resolveEntityDefault";
    private static final String ORACLE_XML_PARSER_XMLPARSER_EXPAND_ENTITY_REF = "oracle.xml.parser.XMLParser.ExpandEntityRef";

    @Override
    public boolean match(Object obj) {
        try {
            if (obj == null) {
                return false;
            }
            if (obj instanceof DocumentBuilder) {
                return true;
            }
            return obj.getClass().getName().contains(".DocumentBuilderImpl");
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public Support getSupport(Object obj) {
        Support support = getDocumentBuilderSupport(obj);
        if (support != Support.UNKNOWN) {
            return support;
        }

        if (matchProxy(obj)) {
            Object samlBuilder = getSamlBuilder(obj);
            if (samlBuilder != null && getSupport(samlBuilder) == Support.DISALLOWED) {
                return Support.DISALLOWED;
            } else {
                return Support.ALLOWED;
            }
        } else if (matchWebLogicJaxp(obj)) {
            Object registryDocumentBuilder = getWebLogicRegistryDocumentBuilder(obj);
            if (registryDocumentBuilder != null
                    && getSupport(registryDocumentBuilder) == Support.DISALLOWED) {
                return Support.DISALLOWED;
            } else {
                return Support.ALLOWED;
            }
        } else if (isOracleJaxp(obj)) {
            if (getOracleJaxpSupport(obj) != Support.DISALLOWED) {
                return Support.DISALLOWED;
            } else {
                return Support.ALLOWED;
            }
        }

        return Support.UNKNOWN;
    }

    private Support getDocumentBuilderSupport(Object obj) {
        try {
            Field domParserField = ReflectUtils.getRecursiveField(obj.getClass(), "domParser");
            if (domParserField == null) {
                return Support.UNKNOWN;
            }
            Object domParser = domParserField.get(obj);
            if (domParser == null) {
                return Support.UNKNOWN;
            }

            Object fConfiguration = getXMLConfiguration(domParser);
            if (fConfiguration == null) {
                return Support.UNKNOWN;
            }

            Class<?> cls = fConfiguration.getClass();
            boolean externalGeneralEntitiesSupport = isSupport(Feature.EXTERNAL_GENERAL, fConfiguration);
            boolean externalParameterEntitiesSupport = isSupport(Feature.EXTERNAL_PARAMETER, fConfiguration);
            boolean loadExternalDTDSupport = isSupport(Feature.LOAD_EXTERNAL_DTD, fConfiguration);

            if (cls.getName().contains(".XIncludeAwareParserConfiguration")) {
                if (isSupport(Feature.XINCLUDE_AWARE, fConfiguration)) {
                    return Support.ALLOWED;
                } else if (isSupport(Feature.DISALLOW_DOCTYPE, fConfiguration)) {
                    return Support.DISALLOWED;
                } else if (!externalGeneralEntitiesSupport
                        && !externalParameterEntitiesSupport
                        && !loadExternalDTDSupport) {
                    return Support.DISALLOWED;
                }
            }

            String fAccessExternalDTD = getFeatureAccessExternalDTDFromSecurityPropertyManager(domParser);
            return getEntityManagerSupport(fConfiguration, fAccessExternalDTD);
        } catch (Throwable e) {
            DongTaiLog.debug("failed to check document builder", e);
        }

        return Support.UNKNOWN;
    }

    private boolean matchProxy(Object obj) {
        return obj != null && obj.getClass().getName().contains("$DocumentBuilderProxy");
    }

    private Object getSamlBuilder(Object obj) {
        try {
            return ReflectUtils.getFieldFromClass(obj.getClass(), "builder").get(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied when reflecting DocumentBuilderProxy.builder {}", e);
            return null;
        } catch (NoSuchFieldException e2) {
            DongTaiLog.debug("Couldn't find builder field on DocumentBuilderProxy");
            return null;
        }
    }

    private boolean matchWebLogicJaxp(Object obj) {
        return obj != null && "weblogic.xml.jaxp.RegistryDocumentBuilder".equals(obj.getClass().getName());
    }

    private Object getWebLogicRegistryDocumentBuilder(Object obj) {
        try {
            return ReflectUtils.getFieldFromClass(obj.getClass(), "builder").get(obj);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Access denied when reflecting RegistryDocumentBuilder.builder {}", e);
            return null;
        } catch (NoSuchFieldException e2) {
            DongTaiLog.debug("Couldn't find builder field on RegistryDocumentBuilder");
            return null;
        }
    }

    private boolean isOracleJaxp(Object obj) {
        return obj != null && "oracle.xml.jaxp.JXDocumentBuilder".equals(obj.getClass().getName());
    }

    private Support getOracleJaxpSupport(Object obj) {
        Support support = Support.UNKNOWN;
        Field domParserField = ReflectUtils.getRecursiveField(obj.getClass(), "domParser");
        if (domParserField == null) {
            return support;
        }
        try {
            Object domParser = domParserField.get(obj);
            if (domParser == null) {
                return support;
            }
            Method method = ReflectUtils.getPublicMethodFromClass(domParser.getClass(), "getAttribute", new Class[]{String.class});
            if (!getOracleJaxpJXDocumentBuilderAttribute(domParser, method, ORACLE_XML_PARSER_XMLPARSER_EXPAND_ENTITY_REF, true)
                    || !getOracleJaxpJXDocumentBuilderAttribute(domParser, method, ORACLE_XDKJAVA_SECURITY_RESOLVE_ENTITY_DEFAULT, true)) {
                return Support.DISALLOWED;
            }
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Failed to inspect JXDocumentBuilder {}", e);
        } catch (NoSuchMethodException e) {
            DongTaiLog.debug("failed to find getAttribute() on JXDocumentBuilder domParser");
        }
        return support;
    }

    private boolean getOracleJaxpJXDocumentBuilderAttribute(Object obj, Method method, String attr, boolean defaultVal) {
        Object ret = null;
        try {
            ret = method.invoke(obj, attr);
        } catch (IllegalAccessException e) {
            DongTaiLog.debug("Problem reflecting JXDocumentBuilderFactory#getAttribute() {}", e);
        } catch (InvocationTargetException e) {
            DongTaiLog.debug("Problem reflecting JXDocumentBuilderFactory#getAttribute() {}", e);
        }
        return (ret != null && (ret instanceof Boolean)) ? (Boolean) ret : defaultVal;
    }
}
