package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;

public class JavaxSAXParserTest extends XXECheckTest {
    private final static String NAME = "JavaxSAXParser";

    @Test
    public void testGetSupport() throws ParserConfigurationException, SAXException {
        SAXParser parser;
        Support support;
        JavaxSAXParserCheck checker = new JavaxSAXParserCheck();
        String realContent = getXXERealContent();

        parser = SAXParserFactory.newInstance().newSAXParser();
        XXEChecker chk = XXECheck.getChecker(parser);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match JavaxSAXParserCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(parser);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(parser));

        parser = SAXParserFactory.newInstance().newSAXParser();
        parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        support = checker.getSupport(parser);
        Assert.assertEquals(NAME + " disallow-doctype-decl", Support.DISALLOWED, support);
        final SAXParser parser1 = parser;
        Assert.assertThrows(NAME + "[C] disallow-doctype-decl", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                getNode(parser1);
            }
        });

        parser = SAXParserFactory.newInstance().newSAXParser();
        parser.getXMLReader().setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        support = checker.getSupport(parser);
        Assert.assertEquals(NAME + " secure-processing", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] secure-processing", realContent, getNode(parser));

        parser = SAXParserFactory.newInstance().newSAXParser();
        parser.getXMLReader().setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        parser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        support = checker.getSupport(parser);
        Assert.assertEquals(NAME + " secure-processing and disallow led", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] secure-processing and disallow led", realContent, getNode(parser));

        parser = SAXParserFactory.newInstance().newSAXParser();
        parser.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.getXMLReader().setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        support = checker.getSupport(parser);
        Assert.assertEquals(NAME + " disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow ege/epe/led", SAFE_OR_BLIND, getNode(parser));

        parser = SAXParserFactory.newInstance().newSAXParser();
        parser.getXMLReader().setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        support = checker.getSupport(parser);
        Assert.assertEquals(NAME + " disallow epe", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow epe", realContent, getNode(parser));
    }

    private String getNode(SAXParser parser) throws SAXException {
        String payload = getPayload();

        try {
            CustomContentHandler handler = new CustomContentHandler();
            parser.parse(new InputSource(new StringReader(payload)), handler);
            String foo = handler.getFoo();
            if (foo.isEmpty()) {
                return SAFE_OR_BLIND;
            }
            return foo;
        } catch (IOException e) {
            return "error: " + e.toString();
        }
    }
}
