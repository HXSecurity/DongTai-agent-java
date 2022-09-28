package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;

public class SAXXMLReaderCheck extends AbstractCheck {
    @Override
    public boolean match(Object obj) {
        if (obj == null) {
            return false;
        }
        return ReflectUtils.isImplementsInterface(obj.getClass(), "org.xml.sax.XMLReader");
    }

    @Override
    public Support getSupport(Object obj) {
        return getXMLParserSupport(obj);
    }
}
