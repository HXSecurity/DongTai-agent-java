package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;

public class DocumentBuilderCheckTest extends XXECheckTest {
    @Test
    public void testGetSupport() throws ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf;
        DocumentBuilder builder;
        Support support;
        String foo;
        DocumentBuilderCheck checker = new DocumentBuilderCheck();
        String realContent = getXXERealContent();

        dbf = DocumentBuilderFactory.newInstance();
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder default", Support.ALLOWED, support);
        Assert.assertEquals("DocumentBuilder content default", realContent, documentBuilderGetNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/xinclude", true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder xinclude", Support.ALLOWED, support);
        // @TODO xinclude payload

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow-doctype-decl", Support.DISALLOWED, support);
        DocumentBuilder finalBuilder1 = builder;
        Assert.assertThrows("DocumentBuilder content disallow-doctype-decl", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                documentBuilderGetNode(finalBuilder1);
            }
        });

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder secure-processing", Support.DISALLOWED, support);
        DocumentBuilder finalBuilder2 = builder;
        Assert.assertThrows("DocumentBuilder content secure-processing", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                documentBuilderGetNode(finalBuilder2);
            }
        });

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder secure-processing & disallow led", Support.ALLOWED, support);
        Assert.assertEquals("DocumentBuilder content secure-processing & disallow led", realContent, documentBuilderGetNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder secure-processing & disallow epe", Support.DISALLOWED, support);
        DocumentBuilder finalBuilder3 = builder;
        Assert.assertThrows("DocumentBuilder content secure-processing & disallow epe", SAXException.class, new ThrowingRunnable() {
            @Override
            public void run() throws SAXException {
                documentBuilderGetNode(finalBuilder3);
            }
        });

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow ege/epe/led", Support.DISALLOWED, support);
        Assert.assertEquals("DocumentBuilder content disallow ege/epe/led", SAFE_OR_BLIND, documentBuilderGetNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow epe/led", Support.ALLOWED, support);
        Assert.assertEquals("DocumentBuilder content disallow epe/led", realContent, documentBuilderGetNode(builder));

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder = dbf.newDocumentBuilder();
        support = checker.getSupport(builder);
        Assert.assertEquals("DocumentBuilder disallow ege", Support.ALLOWED, support);
        Assert.assertEquals("DocumentBuilder content disallow ege", SAFE_OR_BLIND, documentBuilderGetNode(builder));
    }

    private String documentBuilderGetNode(DocumentBuilder builder) throws SAXException {
        String payload = getPayload();

        try {
            Document doc = builder.parse(new InputSource(new StringReader(payload)));

            NodeList nodes = doc.getElementsByTagName("foo");
            if (nodes.getLength() == 0) {
                return "safe empty nodes";
            }
            Node node = nodes.item(0).getFirstChild();
            if (node != null) {
                return node.getNodeValue().replaceAll("\\r\\n?", "\n");
            }
            return SAFE_OR_BLIND;
        } catch (IOException e) {
            return "error: " + e.toString();
        }
    }
}
