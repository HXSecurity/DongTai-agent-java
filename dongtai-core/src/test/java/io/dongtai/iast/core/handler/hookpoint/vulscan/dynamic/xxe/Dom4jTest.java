package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.xml.sax.*;

import java.io.StringReader;

public class Dom4jTest extends XXECheckTest {
    private final static String NAME = "dom4jSAXReader";
    @Test
    public void testGetSupport() throws SAXException {
        SAXReader saxReader;
        XMLReader reader;
        Support support;
        ApacheXMLParserCheck checker = new ApacheXMLParserCheck();
        String realContent = getXXERealContent();

        saxReader = new SAXReader();
        reader = saxReader.getXMLReader();
        XXEChecker chk = XXECheck.getChecker(reader);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match ApacheXMLParserCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(saxReader));

        saxReader = new SAXReader();
        saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        reader = saxReader.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals("XMLReader disallow-doctype-decl", Support.DISALLOWED, support);
        final SAXReader reader1 = saxReader;
        Assert.assertThrows(NAME + "[C] disallow-doctype-decl", SAXParseException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXParseException {
                getNode(reader1);
            }
        });

        saxReader = new SAXReader();
        saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader = saxReader.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow ege/epe/led", SAFE_OR_BLIND, getNode(saxReader));

        saxReader = new SAXReader();
        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader = saxReader.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow epe", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow epe", realContent, getNode(saxReader));

        saxReader = new SAXReader();
        saxReader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        reader = saxReader.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " secure-processing", Support.ALLOWED, support);
        final SAXReader reader2 = saxReader;
        Assert.assertEquals(NAME + "[C] secure-processing", realContent, getNode(reader2));

        saxReader = new SAXReader();
        saxReader.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        saxReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader = saxReader.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " secure-processing and disallow led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] secure-processing and disallow led", realContent, getNode(saxReader));
    }

    private String getNode(org.dom4j.io.SAXReader reader) throws SAXParseException {
        String payload = getPayload();
        try {
            Document document = reader.read(new InputSource(new StringReader(payload)));
            Element root = document.getRootElement();
            if (root.content().size() == 0) {
                return SAFE_OR_BLIND;
            }

            String text = root.getText();
            if (text == null) {
                return SAFE_OR_BLIND;
            }
            return text;
        } catch (DocumentException e) {
            if (e.getCause().getClass() == SAXParseException.class) {
                throw (SAXParseException) e.getCause();
            }
            return "error: " + e.toString();
        }
    }
}
