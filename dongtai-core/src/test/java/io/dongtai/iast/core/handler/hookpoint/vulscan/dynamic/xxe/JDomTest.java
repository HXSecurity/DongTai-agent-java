package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.utils.ReflectUtils;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;

public class JDomTest extends XXECheckTest {
    private final static String NAME = "jDomSAXReader";

    @Test
    public void testGetSupport() throws JDOMException, NoSuchFieldException, IllegalAccessException {
        SAXBuilder saxBuilder;
        Object reader;
        Support support;
        ApacheXMLParserCheck checker = new ApacheXMLParserCheck();
        String realContent = getXXERealContent();
        String node;

        saxBuilder = new SAXBuilder();
        node = getNode(saxBuilder);
        reader = ReflectUtils.getFieldFromClass(saxBuilder.getClass(), "saxParser").get(saxBuilder);
        XXEChecker chk = XXECheck.getChecker(reader);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match ApacheXMLParserCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, node);

        saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        final SAXBuilder builder1 = saxBuilder;
        Assert.assertThrows(NAME + "[C] disallow-doctype-decl", JDOMException.class, new ThrowingRunnable() {
            @Override
            public void run() throws JDOMException {
                getNode(builder1);
            }
        });
        reader = (XMLReader) ReflectUtils.getFieldFromClass(saxBuilder.getClass(), "saxParser").get(saxBuilder);
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow-doctype-decl", Support.DISALLOWED, support);

        saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        node = getNode(saxBuilder);
        reader = (XMLReader) ReflectUtils.getFieldFromClass(saxBuilder.getClass(), "saxParser").get(saxBuilder);
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow ege/epe/led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow ege/epe/led", realContent, node);

        saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        node = getNode(saxBuilder);
        reader = (XMLReader) ReflectUtils.getFieldFromClass(saxBuilder.getClass(), "saxParser").get(saxBuilder);
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow epe", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow epe", realContent, node);

        saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        node = getNode(saxBuilder);
        reader = (XMLReader) ReflectUtils.getFieldFromClass(saxBuilder.getClass(), "saxParser").get(saxBuilder);
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " secure-processing", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] secure-processing", realContent, node);

        saxBuilder = new SAXBuilder();
        saxBuilder.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        node = getNode(saxBuilder);
        reader = (XMLReader) ReflectUtils.getFieldFromClass(saxBuilder.getClass(), "saxParser").get(saxBuilder);
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " secure-processing and disallow led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] secure-processing and disallow led", realContent, node);
    }

    private String getNode(SAXBuilder builder) throws JDOMException {
        String payload = getPayload();
        try {
            Document document = builder.build(new InputSource(new StringReader(payload)));
            Element root = document.getRootElement();
            if (root.getContentSize() == 0) {
                return "safe empty nodes";
            }

            org.jdom.Content node = root.getContent(0);
            if (node == null) {
                return SAFE_OR_BLIND;
            }
            return node.getValue();
        } catch (IOException e) {
            return "error: " + e.toString();
        }
    }
}
