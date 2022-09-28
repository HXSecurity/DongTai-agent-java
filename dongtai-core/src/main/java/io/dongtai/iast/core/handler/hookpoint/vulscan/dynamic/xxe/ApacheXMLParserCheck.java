package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;

public class ApacheXMLParserCheck extends AbstractCheck {
    private static final String ORG_APACHE_XERCES_PARSERS_XMLPARSER = " org.apache.xerces.parsers.XMLParser".substring(1);
    private static final String COM_SUN_ORG_APACHE_XERCES_INTERNAL_PARSERS_XMLPARSER = " com.sun.org.apache.xerces.internal.parsers.XMLParser".substring(1);

    @Override
    public boolean match(Object obj) {
        return ReflectUtils.isDescendantOf(obj.getClass(), ORG_APACHE_XERCES_PARSERS_XMLPARSER)
                || ReflectUtils.isDescendantOf(obj.getClass(), COM_SUN_ORG_APACHE_XERCES_INTERNAL_PARSERS_XMLPARSER);
    }

    @Override
    public Support getSupport(Object obj) {
        return getXMLParserSupport(obj);
    }
}
