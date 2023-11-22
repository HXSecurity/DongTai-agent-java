package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;

public class SAXXMLReaderTest extends XXECheckTest {
    private final static String NAME = "SAXXMLReader";

    @Test
    public void testGetSupport() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf;
        SAXParser parser;
        XMLReader reader;
        Support support;
        ApacheXMLParserCheck checker = new ApacheXMLParserCheck();
        String realContent = getXXERealContent();

        spf = SAXParserFactory.newInstance();
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        XXEChecker chk = XXECheck.getChecker(reader);
        Assert.assertNotNull(chk);
        Assert.assertEquals(NAME + " match ApacheXMLParserCheck", checker.getClass(), chk.getClass());
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " default", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] default", realContent, getNode(reader));

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow-doctype-decl", Support.DISALLOWED, support);
        final XMLReader reader1 = reader;
        Assert.assertThrows(NAME + "[C] disallow-doctype-decl", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                getNode(reader1);
            }
        });

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow ege/epe/led", SAFE_OR_BLIND, getNode(reader));

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " disallow epe", Support.ALLOWED, support);
        Assert.assertEquals(NAME + "[C] disallow epe", realContent, getNode(reader));

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " secure-processing", Support.DISALLOWED, support);
        final XMLReader reader2 = reader;
        Assert.assertThrows(NAME + "[C] secure-processing", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                getNode(reader2);
            }
        });

        spf = SAXParserFactory.newInstance();
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();
        support = checker.getSupport(reader);
        Assert.assertEquals(NAME + " secure-processing and disallow led", Support.ALLOWED, support);
//        Assert.assertEquals(NAME + "[C] secure-processing and disallow led", realContent, getNode(reader));
    }

    private String getNode(XMLReader reader) throws SAXException {
        String payload = getPayload();

        try {
            CustomContentHandler handler = new CustomContentHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new StringReader(payload)));
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
